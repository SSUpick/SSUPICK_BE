package com.ssupick.ssupick_be.domain.payment.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.payment.client.PortOneClient;
import com.ssupick.ssupick_be.domain.payment.dto.request.PaymentVerifyRequest;
import com.ssupick.ssupick_be.domain.payment.dto.response.CouponProductResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PortOnePaymentResponse;
import com.ssupick.ssupick_be.domain.payment.entity.Payment;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import com.ssupick.ssupick_be.domain.payment.enums.PaymentStatus;
import com.ssupick.ssupick_be.domain.payment.properties.PortOneProperties;
import com.ssupick.ssupick_be.domain.payment.repository.PaymentRepository;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PAID_STATUS = "PAID";

    private final PortOneClient portOneClient;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentWriter paymentWriter;
    private final PortOneProperties portOneProperties;

    // 프론트 결제 화면에 표시할 쿠폰 상품 목록을 조회합니다.
    @Transactional(readOnly = true)
    public List<CouponProductResponse> getCouponProducts() {
        return Arrays.stream(CouponProduct.values())
                .map(CouponProductResponse::from)
                .toList();
    }

    // 외부 API 호출과 검증은 트랜잭션 밖에서 수행하고, DB 반영은 PaymentWriter에 위임합니다.
    public PaymentVerifyResponse verifyPayment(Long userId, String paymentId, PaymentVerifyRequest request) {
        CouponProduct couponProduct = request.couponProduct();

        // 1. DB에서 기존 결제 확인 — 이미 PAID면 PortOne 호출 없이 멱등 성공으로 반환합니다.
        Optional<Payment> existingPayment = paymentRepository.findByPaymentIdAndUserId(paymentId, userId);
        if (existingPayment.isPresent() && existingPayment.get().getStatus() == PaymentStatus.PAID) {
            Payment payment = existingPayment.get();
            int remainingCouponCount = userRepository.findById(userId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND))
                    .getRemainingCouponCount();
            return new PaymentVerifyResponse(
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    payment.getPaidAmount(),
                    0,
                    remainingCouponCount
            );
        }

        // 2. PortOne 외부 API 호출 + 순수 검증 (트랜잭션 밖)
        PortOnePaymentResponse portOnePayment = portOneClient.getPayment(paymentId);
        validatePayment(portOnePayment, couponProduct);

        // 3. DB 저장 + 쿠폰 충전 (트랜잭션 안)
        return paymentWriter.completePayment(userId, paymentId, couponProduct, portOnePayment);
    }

    // PortOne Browser SDK를 로드하자마자 결제창을 여는 최소 HTML을 생성합니다.
    @Transactional
    public String buildCheckoutHtml(Long userId, CouponProduct couponProduct) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()) {
            throw new GeneralException(ErrorStatus.PAYMENT_PHONE_NUMBER_REQUIRED);
        }

        String paymentId = generatePaymentId(userId);
        paymentRepository.save(Payment.ready(paymentId, user, couponProduct));

        String storeId = jsString(portOneProperties.storeId());
        String channelKey = jsString(portOneProperties.channelKey());
        String redirectUrl = jsString(portOneProperties.redirectUrl());
        String safePaymentId = jsString(paymentId);
        String orderName = jsString(couponProduct.getOrderName());
        String productCode = jsString(couponProduct.name());
        String customerName = jsString(user.getName() != null ? user.getName() : "테스트 유저");
        String customerEmail = jsString(user.getEmail() != null ? user.getEmail() : "test@example.com");
        String customerPhoneNumber = jsString(user.getPhoneNumber());

        return """
                <!doctype html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>SSUPICK 쿠폰 결제</title>
                  <script src="https://cdn.portone.io/v2/browser-sdk.js"></script>
                </head>
                <body>
                  <noscript>결제를 진행하려면 JavaScript를 활성화해야 합니다.</noscript>
                  <script>
                    const paymentId = "%s";
                    const couponProduct = "%s";

                    window.addEventListener("load", async () => {
                      const response = await PortOne.requestPayment({
                        storeId: "%s",
                        channelKey: "%s",
                        paymentId,
                        orderName: "%s",
                        totalAmount: %d,
                        currency: "KRW",
                        payMethod: "CARD",
                        redirectUrl: "%s",
                        customer: {
                          fullName: "%s",
                          email: "%s",
                          phoneNumber: "%s"
                        }
                      });

                      console.log("paymentId:", paymentId);
                      console.log("couponProduct:", couponProduct);
                      console.log("response:", response);
                    });
                  </script>
                </body>
                </html>
                """.formatted(
                safePaymentId,
                productCode,
                storeId,
                channelKey,
                orderName,
                couponProduct.getPrice(),
                redirectUrl,
                customerName,
                customerEmail,
                customerPhoneNumber
        );
    }

    // PortOne 응답의 결제 상태와 금액을 검증합니다.
    private void validatePayment(PortOnePaymentResponse payment, CouponProduct couponProduct) {
        if (!PAID_STATUS.equals(payment.status())) {
            throw new GeneralException(ErrorStatus.PAYMENT_STATUS_INVALID);
        }
        Long actualAmount = payment.amount() != null ? payment.amount().total() : null;
        if (!couponProduct.getPrice().equals(actualAmount)) {
            throw new GeneralException(ErrorStatus.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private String jsString(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("</", "<\\/")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\u2028", "\\u2028")
                .replace("\u2029", "\\u2029");
    }

    private String generatePaymentId(Long userId) {
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        return "cp" + userId + "_" + randomPart;
    }

    public void deleteByUser(User user){
        paymentRepository.deleteAllByUser(user);
    }
}
