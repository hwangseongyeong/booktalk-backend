package com.booktalk.domain.user;

import com.booktalk.domain.user.dto.UserProfileResponse;
import com.booktalk.domain.user.dto.UserUpdateRequest;
import com.booktalk.global.security.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final CurrentUserResolver currentUserResolver;

    public UserProfileResponse getMyProfile() {
        return UserProfileResponse.from(currentUserResolver.getCurrentUser());
    }

    /** 닉네임/프로필 수정. 온보딩 1~2단계에서도 이 API로 저장한다. */
    @Transactional
    public UserProfileResponse updateMyProfile(UserUpdateRequest request) {
        User user = currentUserResolver.getCurrentUser();
        user.editProfile(request.nickname(), request.profileImageUrl(), request.profileColor());
        return UserProfileResponse.from(user);
    }

    /** 온보딩(닉네임 → 프로필 → 친구초대) 완료 표시. 이후 로그인은 소셜 프로필로 덮어쓰지 않는다. */
    @Transactional
    public UserProfileResponse completeOnboarding() {
        User user = currentUserResolver.getCurrentUser();
        user.completeOnboarding();
        return UserProfileResponse.from(user);
    }
}
