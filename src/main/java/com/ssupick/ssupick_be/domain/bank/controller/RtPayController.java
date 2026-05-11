package com.ssupick.ssupick_be.domain.bank.controller;

import com.ssupick.ssupick_be.domain.bank.dto.response.RtPayCheckResponse;
import com.ssupick.ssupick_be.domain.bank.service.RtPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class RtPayController {

    private final RtPayService rtPayService;

    @RequestMapping("/checkpay.jsp")
    public ResponseEntity<RtPayCheckResponse> check(HttpServletRequest request) {
        RtPayCheckResponse response = rtPayService.check(extractParameters(request), request.getRequestURL().toString());
        return ResponseEntity.ok(response);
    }

    private Map<String, String> extractParameters(HttpServletRequest request) {
        if (request.getParameterMap().isEmpty()) {
            return Collections.emptyMap();
        }

        return request.getParameterMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().length > 0 ? entry.getValue()[0] : ""
                ));
    }
}
