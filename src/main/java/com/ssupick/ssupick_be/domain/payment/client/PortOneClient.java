package com.ssupick.ssupick_be.domain.payment.client;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.payment.dto.response.PortOnePaymentResponse;
import com.ssupick.ssupick_be.domain.payment.properties.PortOneProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PortOneClient {

    private final WebClient webClient;
    private final PortOneProperties portOneProperties;

    @Autowired
    public PortOneClient(PortOneProperties portOneProperties) {
        this(WebClient.builder()
                .baseUrl(portOneProperties.resolvedBaseUrl())
                .build(), portOneProperties);
    }

    PortOneClient(WebClient webClient, PortOneProperties portOneProperties) {
        this.webClient = webClient;
        this.portOneProperties = portOneProperties;
    }

    public PortOnePaymentResponse getPayment(String paymentId) {
        try {
            PortOnePaymentResponse response = webClient.get()
                    .uri("/payments/{paymentId}", paymentId)
                    .header("Authorization", "PortOne " + portOneProperties.apiSecret())
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            r -> r.bodyToMono(String.class)
                                    .flatMap(body -> handleError(r.statusCode(), body))
                    )
                    .bodyToMono(PortOnePaymentResponse.class)
                    .block();

            if (response == null || response.id() == null) {
                throw new GeneralException(ErrorStatus.PORTONE_REQUEST_FAILED);
            }
            return response;
        } catch (WebClientResponseException e) {
            log.error("[PortOne] 결제 조회 실패 status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new GeneralException(ErrorStatus.PORTONE_REQUEST_FAILED);
        }
    }

    private Mono<Throwable> handleError(HttpStatusCode statusCode, String body) {
        log.error("[PortOne] API 실패 status={}, body={}", statusCode, body);
        if (statusCode == HttpStatus.NOT_FOUND) {
            return Mono.error(new GeneralException(ErrorStatus.PAYMENT_NOT_FOUND));
        }
        return Mono.error(new GeneralException(ErrorStatus.PORTONE_REQUEST_FAILED));
    }
}
