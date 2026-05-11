package com.ssupick.ssupick_be.domain.bank.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bank.webhook")
public record BankWebhookProperties(
        String secret,
        String rtpKey
) {}
