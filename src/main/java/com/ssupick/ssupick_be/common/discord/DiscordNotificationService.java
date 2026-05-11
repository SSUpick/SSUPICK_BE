package com.ssupick.ssupick_be.common.discord;

import com.ssupick.ssupick_be.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordNotificationService {

    private final DiscordProperties discordProperties;
    private final WebClient webClient;

    public void notifyCouponCharged(
            String depositorName,
            Long amount,
            int chargedCouponCount,
            User user
    ) {
        if (!StringUtils.hasText(discordProperties.couponWebhookUrl())) {
            return;
        }

        try {
            webClient.post()
                    .uri(discordProperties.couponWebhookUrl())
                    .bodyValue(buildCouponChargedPayload(depositorName, amount, chargedCouponCount, user))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.warn("[Discord] 쿠폰 충전 알림 전송 실패 - userId: {}", user.getId(), e);
        }
    }

    private Map<String, Object> buildCouponChargedPayload(
            String depositorName,
            Long amount,
            int chargedCouponCount,
            User user
    ) {
        return Map.of(
                "content", "쿠폰 충전이 완료되었습니다.",
                "embeds", List.of(Map.of(
                        "title", "쿠폰 충전 완료",
                        "color", 0x4CAF50,
                        "fields", List.of(
                                field("입금자", valueOrDash(depositorName), true),
                                field("입금액", amount != null ? amount + "원" : "-", true),
                                field("충전 쿠폰 수", chargedCouponCount + "개", true),
                                field("유저 ID", String.valueOf(user.getId()), true),
                                field("이름", valueOrDash(user.getName()), true),
                                field("닉네임", valueOrDash(user.getNickname()), true),
                                field("현재 쿠폰 수", user.getRemainingCouponCount() + "개", true)
                        )
                ))
        );
    }

    private Map<String, Object> field(String name, String value, boolean inline) {
        return Map.of(
                "name", name,
                "value", value,
                "inline", inline
        );
    }

    private String valueOrDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }
}
