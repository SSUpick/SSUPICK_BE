package com.ssupick.ssupick_be.common.discord;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discord")
public record DiscordProperties(
        String couponWebhookUrl
) {}
