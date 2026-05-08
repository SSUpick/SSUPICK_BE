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

    // PortOne 기본 API URL을 사용하는 WebClient를 생성합니다.
    @Autowired
    public PortOneClient(PortOneProperties portOneProperties) {
        this(WebClient.builder()
                .baseUrl(portOneProperties.resolvedBaseUrl())
                .build(), portOneProperties);
    }

    // 테스트에서 주입한 WebClient로 PortOneClient를 생성합니다.
    PortOneClient(WebClient webClient, PortOneProperties portOneProperties) {
        this.webClient = webClient;
        this.portOneProperties = portOneProperties;
    }

    // paymentId로 PortOne 결제 단건을 조회합니다.
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

    // PortOne API 에러 응답을 프로젝트 공통 예외로 변환합니다.
    private Mono<Throwable> handleError(HttpStatusCode statusCode, String body) {
        log.error("[PortOne] API 실패 status={}, body={}", statusCode, body);
        if (statusCode == HttpStatus.NOT_FOUND) {
            return Mono.error(new GeneralException(ErrorStatus.PAYMENT_NOT_FOUND));
        }
        return Mono.error(new GeneralException(ErrorStatus.PORTONE_REQUEST_FAILED));
    }
}
