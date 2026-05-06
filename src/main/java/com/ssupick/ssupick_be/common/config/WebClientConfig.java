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
     * - 이미지 생성은 수십 초 소요될 수 있으므로 타임아웃 60초로 설정
     * - baseUrl 없이 생성 (GeminiImageClient에서 전체 URL 직접 사용)
     */
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)   // 연결 타임아웃 10초
                .responseTimeout(Duration.ofSeconds(60))                 // 응답 타임아웃 60초
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))   // 응답 버퍼 10MB (이미지 데이터)
                .build();
    }
}
