package com.ssupick.ssupick_be.domain.bank.service;

import com.ssupick.ssupick_be.domain.bank.client.RtPayClient;
import com.ssupick.ssupick_be.domain.bank.dto.request.BankDepositWebhookRequest;
import com.ssupick.ssupick_be.domain.bank.dto.response.BankDepositWebhookResponse;
import com.ssupick.ssupick_be.domain.bank.dto.response.RtPayCheckResponse;
import com.ssupick.ssupick_be.domain.bank.properties.BankWebhookProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RtPayService {

    private final BankWebhookProperties bankWebhookProperties;
    private final RtPayClient rtPayClient;
    private final BankDepositService bankDepositService;

    public RtPayCheckResponse check(Map<String, String> parameters, String requestUrl) {
        if (parameters.isEmpty()) {
            rtPayClient.setPath(requestUrl);
            return new RtPayCheckResponse("400", "NO");
        }

        if (!isValidKey(parameters.get("regPkey"))) {
            return new RtPayCheckResponse("400", "NO");
        }

        Map<String, Object> rtPayResponse = rtPayClient.checkPay(parameters);
        String rcode = asString(rtPayResponse.get("RCODE"));
        if (!"200".equals(rcode)) {
            return new RtPayCheckResponse(rcode, "NO");
        }

        BankDepositWebhookResponse response = bankDepositService.receiveVerified(new BankDepositWebhookRequest(
                buildEventKey(rtPayResponse),
                asString(rtPayResponse.get("RNAME")),
                parseAmount(rtPayResponse.get("RPAY")),
                LocalDateTime.now(),
                asString(rtPayResponse.get("RTEXT"))
        ));

        String pchk = "PROCESSED".equals(response.status()) ? "OK" : "NO";
        return new RtPayCheckResponse("200", pchk);
    }

    private boolean isValidKey(String presentedKey) {
        String expectedKey = bankWebhookProperties.rtpKey();
        return expectedKey != null && presentedKey != null && MessageDigest.isEqual(
                expectedKey.getBytes(),
                presentedKey.getBytes()
        );
    }

    private String buildEventKey(Map<String, Object> rtPayResponse) {
        String raw = asString(rtPayResponse.get("RBANK")) + "|"
                + asString(rtPayResponse.get("RNAME")) + "|"
                + asString(rtPayResponse.get("RPAY")) + "|"
                + asString(rtPayResponse.get("RTEXT"));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes());
            return "rtpay-" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", e);
        }
    }

    private Long parseAmount(Object value) {
        String amount = asString(value).replace(",", "").trim();
        return Long.parseLong(amount);
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
