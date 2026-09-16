#!/usr/bin/env bash
#
# BookTalk 책등 이미지용 S3 버킷 셋업 스크립트 (방식 B: 버킷 직접 공개)
#
# 하는 일:
#   1) S3 버킷 생성 (이미 있으면 건너뜀)
#   2) Block Public Access 해제 (spines/* 공개 읽기를 위해)
#   3) 버킷 정책 적용 (spines/* 누구나 GetObject 가능)
#   4) 업로드용 IAM User + s3:PutObject 정책 생성
#   5) Access Key 발급 → application-local.yml에 넣을 값 출력
#
# 사전 준비:
#   - AWS CLI 설치 및 `aws configure`로 관리자 권한 자격증명 설정
#   - jq 설치 (Access Key 파싱용)
#
# 사용법:
#   ./scripts/aws/setup-s3.sh
#   BUCKET=my-bucket REGION=ap-northeast-2 ./scripts/aws/setup-s3.sh   # 값 오버라이드
#
# 멱등성: 이미 존재하는 리소스는 건너뛰거나 덮어씁니다. 여러 번 실행해도 안전합니다.
#   (단, 5번 Access Key는 실행 시마다 새로 발급되므로 한 IAM User당 최대 2개 제한에 유의)

set -euo pipefail

BUCKET="${BUCKET:-booktalk-spine-images}"
REGION="${REGION:-ap-northeast-2}"
IAM_USER="${IAM_USER:-booktalk-spine-uploader}"
IAM_POLICY_NAME="${IAM_POLICY_NAME:-booktalk-spine-putobject}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PUBLIC_BASE_URL="https://${BUCKET}.s3.${REGION}.amazonaws.com"

# 치환한 정책 JSON을 임시 파일에 쓰고 file:// 로 전달한다.
# (인라인 문자열로 넘기면 CLI가 JSON을 잘못 파싱해 "Principal cannot be empty" 오류가 날 수 있음)
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

echo "==> 설정"
echo "    BUCKET   = ${BUCKET}"
echo "    REGION   = ${REGION}"
echo "    IAM_USER = ${IAM_USER}"
echo

# ---------------------------------------------------------------------------
# 1) 버킷 생성
# ---------------------------------------------------------------------------
echo "==> [1/5] 버킷 생성"
if aws s3api head-bucket --bucket "${BUCKET}" 2>/dev/null; then
  echo "    이미 존재함: ${BUCKET} (건너뜀)"
else
  # us-east-1은 LocationConstraint를 넣으면 에러가 나므로 분기
  if [ "${REGION}" = "us-east-1" ]; then
    aws s3api create-bucket --bucket "${BUCKET}" --region "${REGION}"
  else
    aws s3api create-bucket --bucket "${BUCKET}" --region "${REGION}" \
      --create-bucket-configuration "LocationConstraint=${REGION}"
  fi
  echo "    생성 완료: ${BUCKET}"
fi

# ---------------------------------------------------------------------------
# 2) Block Public Access 해제
# ---------------------------------------------------------------------------
echo "==> [2/5] Block Public Access 해제"
aws s3api put-public-access-block --bucket "${BUCKET}" \
  --public-access-block-configuration \
  "BlockPublicAcls=false,IgnorePublicAcls=false,BlockPublicPolicy=false,RestrictPublicBuckets=false"
echo "    완료"

# ---------------------------------------------------------------------------
# 3) 버킷 정책 적용 (spines/* 공개 읽기)
# ---------------------------------------------------------------------------
echo "==> [3/5] 버킷 정책 적용"
sed "s/__BUCKET__/${BUCKET}/g" "${SCRIPT_DIR}/bucket-policy.json" > "${TMP_DIR}/bucket-policy.json"
aws s3api put-bucket-policy --bucket "${BUCKET}" --policy "file://${TMP_DIR}/bucket-policy.json"
echo "    완료 (spines/* GetObject 공개)"

# ---------------------------------------------------------------------------
# 4) 업로드용 IAM User + 인라인 정책
# ---------------------------------------------------------------------------
echo "==> [4/5] IAM User 및 s3:PutObject 정책"
if aws iam get-user --user-name "${IAM_USER}" >/dev/null 2>&1; then
  echo "    IAM User 이미 존재함: ${IAM_USER} (건너뜀)"
else
  aws iam create-user --user-name "${IAM_USER}" >/dev/null
  echo "    IAM User 생성: ${IAM_USER}"
fi

sed "s/__BUCKET__/${BUCKET}/g" "${SCRIPT_DIR}/iam-putobject-policy.json" > "${TMP_DIR}/iam-putobject-policy.json"
aws iam put-user-policy --user-name "${IAM_USER}" \
  --policy-name "${IAM_POLICY_NAME}" \
  --policy-document "file://${TMP_DIR}/iam-putobject-policy.json"
echo "    인라인 정책 적용: ${IAM_POLICY_NAME}"

# ---------------------------------------------------------------------------
# 5) Access Key 발급
# ---------------------------------------------------------------------------
echo "==> [5/5] Access Key 발급"
KEY_JSON="$(aws iam create-access-key --user-name "${IAM_USER}")"
ACCESS_KEY_ID="$(echo "${KEY_JSON}" | jq -r '.AccessKey.AccessKeyId')"
SECRET_ACCESS_KEY="$(echo "${KEY_JSON}" | jq -r '.AccessKey.SecretAccessKey')"

cat <<EOF

============================================================
셋업 완료. 아래 값을 application-local.yml 의 s3 블록에 넣으세요.
(SecretAccessKey는 지금만 확인 가능합니다. 반드시 저장하세요.)
============================================================

storage:
  mode: s3

s3:
  region: ${REGION}
  bucket: ${BUCKET}
  access-key: ${ACCESS_KEY_ID}
  secret-key: ${SECRET_ACCESS_KEY}
  public-base-url: ${PUBLIC_BASE_URL}

검증:
  앱 실행 후 책 읽기 시작 → 아래 URL이 열리면 정상
  ${PUBLIC_BASE_URL}/spines/{bookId}.svg
============================================================
EOF
