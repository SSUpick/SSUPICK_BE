package com.ssupick.ssupick_be.domain.bank.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssupick.ssupick_be.domain.bank.properties.BankWebhookProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RtPayClient {

    private static final String CHECK_PAY_URL = "https://rtpay.net/CheckPay/checkpay.php";
    private static final String TEST_CHECK_PAY_URL = "https://rtpay.net/CheckPay/test_checkpay.php";
    private static final String SET_PURL_URL = "https://rtpay.net/CheckPay/setPurl.php";

    private final WebClient webClient;
    private final BankWebhookProperties bankWebhookProperties;
    private final ObjectMapper objectMapper;

    public Map<String, Object> checkPay(Map<String, String> parameters) {
        String targetUrl = resolveCheckPayUrl(parameters);
        return postForm(targetUrl, toFormData(parameters));
    }

    public Map<String, Object> setPath(String referer) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("regPkey", bankWebhookProperties.rtpKey());
        formData.add("Referer", referer);
        return postForm(SET_PURL_URL, formData);
    }

    private String resolveCheckPayUrl(Map<String, String> parameters) {
        String ugrd = parameters.get("ugrd");
        if ("11".equals(ugrd) || "12".equals(ugrd)) {
            return TEST_CHECK_PAY_URL;
        }

        return CHECK_PAY_URL;
    }

    private MultiValueMap<String, String> toFormData(Map<String, String> parameters) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        parameters.forEach(formData::add);
        return formData;
    }

    private Map<String, Object> postForm(String url, MultiValueMap<String, String> formData) {
        String responseBody = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            return objectMapper.readValue(responseBody, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of(
                    "RCODE", "600",
                    "EMSG", e.getMessage(),
                    "RBODY", responseBody == null ? "" : responseBody
            );
        }
    }
}
