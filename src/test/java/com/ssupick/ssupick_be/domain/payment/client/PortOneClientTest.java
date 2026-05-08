package com.ssupick.ssupick_be.domain.payment.client;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.payment.dto.response.PortOnePaymentResponse;
import com.ssupick.ssupick_be.domain.payment.properties.PortOneProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortOneClientTest {

    private MockWebServer mockWebServer;
    private PortOneClient portOneClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        PortOneProperties properties = new PortOneProperties(
                mockWebServer.url("/").toString(),
                "test_api_secret",
                "test_store_id",
                "test_channel_key"
        );
        portOneClient = new PortOneClient(properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getPayment_sendsPortOneAuthorizationHeaderAndParsesResponse() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "id": "payment-1",
                          "status": "PAID",
                          "amount": {
                            "total": 4900
                          }
                        }
                        """));

        PortOnePaymentResponse response = portOneClient.getPayment("payment-1");

        assertThat(response.id()).isEqualTo("payment-1");
        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.amount().total()).isEqualTo(4900L);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/payments/payment-1");
        assertThat(request.getHeader("Authorization")).isEqualTo("PortOne test_api_secret");
    }

    @Test
    void getPayment_throwsPaymentNotFoundWhenPortOneReturns404() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "type": "NOT_FOUND",
                          "message": "payment not found"
                        }
                        """));

        assertThatThrownBy(() -> portOneClient.getPayment("missing-payment"))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.PAYMENT_NOT_FOUND));
    }
}
