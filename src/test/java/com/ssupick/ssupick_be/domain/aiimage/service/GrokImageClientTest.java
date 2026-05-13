package com.ssupick.ssupick_be.domain.aiimage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssupick.ssupick_be.domain.aiimage.properties.XaiProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class GrokImageClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void editImage_sendsImageEditRequestAndDownloadsReturnedUrl() throws Exception {
        byte[] generatedImageBytes = "generated-image".getBytes();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "data": [
                            {
                              "url": "%s/generated.jpg",
                              "mime_type": "image/jpeg",
                              "revised_prompt": ""
                            }
                          ]
                        }
                        """.formatted(mockWebServer.url("/").toString().replaceAll("/$", ""))));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", MediaType.IMAGE_JPEG_VALUE)
                .setBody(new okio.Buffer().write(generatedImageBytes)));

        GrokImageClient client = new GrokImageClient(
                new XaiProperties("test-api-key", mockWebServer.url("/").toString().replaceAll("/$", ""), "grok-imagine-image-quality", null),
                WebClient.builder().build()
        );

        byte[] result = client.editImage("original-image".getBytes(), MediaType.IMAGE_JPEG_VALUE, "동물의 숲 스타일");

        assertThat(result).isEqualTo(generatedImageBytes);

        RecordedRequest editRequest = mockWebServer.takeRequest();
        assertThat(editRequest.getMethod()).isEqualTo("POST");
        assertThat(editRequest.getPath()).isEqualTo("/v1/images/edits");
        assertThat(editRequest.getHeader("Authorization")).isEqualTo("Bearer test-api-key");

        JsonNode requestBody = objectMapper.readTree(editRequest.getBody().readUtf8());
        assertThat(requestBody.get("model").asText()).isEqualTo("grok-imagine-image-quality");
        assertThat(requestBody.get("prompt").asText()).isEqualTo("동물의 숲 스타일");
        assertThat(requestBody.get("image").get("type").asText()).isEqualTo("image_url");
        assertThat(requestBody.get("image").get("url").asText())
                .isEqualTo("data:image/jpeg;base64," + Base64.getEncoder().encodeToString("original-image".getBytes()));

        RecordedRequest downloadRequest = mockWebServer.takeRequest();
        assertThat(downloadRequest.getMethod()).isEqualTo("GET");
        assertThat(downloadRequest.getPath()).isEqualTo("/generated.jpg");
    }

    @Test
    void editImage_returnsBase64ImageWhenResponseContainsB64Json() {
        byte[] generatedImageBytes = "generated-image".getBytes();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "data": [
                            {
                              "b64_json": "%s",
                              "mime_type": "image/jpeg",
                              "revised_prompt": ""
                            }
                          ]
                        }
                        """.formatted(Base64.getEncoder().encodeToString(generatedImageBytes))));

        GrokImageClient client = new GrokImageClient(
                new XaiProperties("test-api-key", mockWebServer.url("/").toString().replaceAll("/$", ""), "grok-imagine-image-quality", null),
                WebClient.builder().build()
        );

        byte[] result = client.editImage("original-image".getBytes(), MediaType.IMAGE_PNG_VALUE, "동물의 숲 스타일");

        assertThat(result).isEqualTo(generatedImageBytes);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    }
}
