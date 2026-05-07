package com.ssupick.ssupick_be.common.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient kakaoAuthWebClient() {
        return WebClient.builder()
                .baseUrl("https://kauth.kakao.com")
                .build();
    }

    @Bean
    public WebClient kakaoApiWebClient() {
        return WebClient.builder()
                .baseUrl("https://kapi.kakao.com")
                .build();
    }

    /**
     * Gemini API 전용 WebClient
     * - 이미지 생성 모델 특성상 응답이 느림 → 타임아웃 120초로 설정
     * - baseUrl 없이 생성 (GeminiImageClient에서 전체 URL 직접 사용)
     */
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)    // 연결 타임아웃 10초
                .responseTimeout(Duration.ofSeconds(120))                 // 응답 타임아웃 120초
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(120, TimeUnit.SECONDS))  // 읽기 타임아웃 120초
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))); // 쓰기 타임아웃 30초 (이미지 전송)

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(20 * 1024 * 1024))   // 응답 버퍼 20MB
                .build();
    }
}
