package com.booktalk.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 실행용 설정. 표지 이미지 다운로드 같은 느린 외부 I/O를 요청 스레드/트랜잭션 밖에서 처리해
 * DB 커넥션이 외부 호출 동안 점유되는 것을 막는다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 책등 색상 추출(표지 다운로드 포함) 전용 풀. 외부 CDN 지연이 전체로 번지지 않도록
     * 동시 실행 수를 제한하고, 큐가 가득 차면 호출 스레드에서 처리(CallerRuns)해 유실을 막는다.
     */
    @Bean(name = "spineExecutor")
    public Executor spineExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("spine-");
        executor.setRejectedExecutionHandler(
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
