package com.ssupick.ssupick_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SsupickBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsupickBeApplication.class, args);
    }
}
