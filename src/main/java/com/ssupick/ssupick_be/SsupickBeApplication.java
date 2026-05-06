package com.ssupick.ssupick_be;

import com.ssupick.ssupick_be.domain.oauth.properties.KakaoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties(KakaoProperties.class)
public class SsupickBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsupickBeApplication.class, args);
    }
}
