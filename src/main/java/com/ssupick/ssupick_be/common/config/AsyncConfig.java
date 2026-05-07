package com.ssupick.ssupick_be.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * AI 이미지 생성 전용 스레드 풀
     *
     * SimpleAsyncTaskExecutor(기본값)는 요청마다 새 스레드를 무한 생성해서
     * 동시 요청이 몰리면 서버 자원 고갈 위험이 있음.
     * ThreadPoolTaskExecutor로 스레드 수를 제한해서 안전하게 처리.
     *
     * - corePoolSize:  평상시 유지할 스레드 수
     * - maxPoolSize:   최대 스레드 수 (큐가 꽉 찼을 때 추가 생성)
     * - queueCapacity: 스레드가 maxPoolSize일 때 대기할 요청 수
     */
    @Bean(name = "aiImageExecutor")
    public Executor aiImageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("ai-image-");
        executor.initialize();
        return executor;
    }
}
