package com.ssupick.ssupick_be;

import com.ssupick.ssupick_be.domain.aiimage.properties.GeminiProperties;
import com.ssupick.ssupick_be.domain.admin.properties.AdminProperties;
import com.ssupick.ssupick_be.domain.oauth.properties.KakaoProperties;
import com.ssupick.ssupick_be.domain.payment.properties.PortOneProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties({
        KakaoProperties.class,
        PortOneProperties.class,
        GeminiProperties.class,
        AdminProperties.class
})
public class SsupickBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsupickBeApplication.class, args);
    }
}
