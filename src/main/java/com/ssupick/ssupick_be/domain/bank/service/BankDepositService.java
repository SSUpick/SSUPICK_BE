package com.ssupick.ssupick_be.domain.bank.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.admin.entity.AdminCouponAdjustment;
import com.ssupick.ssupick_be.domain.admin.repository.AdminCouponAdjustmentRepository;
import com.ssupick.ssupick_be.domain.bank.dto.request.BankDepositAssignRequest;
import com.ssupick.ssupick_be.domain.bank.dto.request.BankDepositWebhookRequest;
import com.ssupick.ssupick_be.domain.bank.dto.response.BankDepositEventResponse;
import com.ssupick.ssupick_be.domain.bank.dto.response.BankDepositWebhookResponse;
import com.ssupick.ssupick_be.domain.bank.entity.BankDepositEvent;
import com.ssupick.ssupick_be.domain.bank.enums.BankDepositEventStatus;
import com.ssupick.ssupick_be.domain.bank.properties.BankWebhookProperties;
import com.ssupick.ssupick_be.domain.bank.repository.BankDepositEventRepository;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankDepositService {

    private static final String BANK_NAME = "KAKAOBANK";

    private final BankWebhookProperties bankWebhookProperties;
    private final BankDepositEventRepository bankDepositEventRepository;
    private final UserRepository userRepository;
    private final AdminCouponAdjustmentRepository adminCouponAdjustmentRepository;

    @Transactional
    public BankDepositWebhookResponse receive(String webhookSecret, BankDepositWebhookRequest request) {
        validateWebhookSecret(webhookSecret);
        return receiveVerified(request);
    }

    @Transactional
    public BankDepositWebhookResponse receiveVerified(BankDepositWebhookRequest request) {
        return bankDepositEventRepository.findByEventKey(request.eventKey())
                .map(event -> new BankDepositWebhookResponse(event.getId(), event.getStatus().name(), true))
                .orElseGet(() -> processNewEvent(request));
    }

    @Transactional(readOnly = true)
    public List<BankDepositEventResponse> getPendingEvents() {
        return bankDepositEventRepository.findAllByStatusInOrderByCreatedAtDesc(List.of(
                        BankDepositEventStatus.PENDING,
                        BankDepositEventStatus.ADMIN_REQUIRED,
                        BankDepositEventStatus.AUTO_MATCHED
                ))
                .stream()
                .map(BankDepositEventResponse::from)
                .toList();
    }

    @Transactional
    public BankDepositEventResponse assign(Long eventId, BankDepositAssignRequest request) {
        BankDepositEvent event = bankDepositEventRepository.findById(eventId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND));
        if (event.getStatus() == BankDepositEventStatus.PROCESSED) {
            return BankDepositEventResponse.from(event);
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        CouponProduct couponProduct = request.couponProduct() != null
                ? request.couponProduct()
                : event.getCouponProduct();
        if (couponProduct == null) {
            throw new GeneralException(ErrorStatus.BAD_REQUEST);
        }

        chargeCoupon(event, user, couponProduct, "BANK_TRANSFER_ADMIN_ASSIGN");
        return BankDepositEventResponse.from(event);
    }

    private BankDepositWebhookResponse processNewEvent(BankDepositWebhookRequest request) {
        CouponProduct couponProduct = resolveCouponProduct(request.amount());
        BankDepositEvent event = BankDepositEvent.pending(
                request.eventKey(),
                BANK_NAME,
                request.depositorName(),
                request.amount(),
                request.depositedAt(),
                request.rawText(),
                couponProduct
        );
        BankDepositEvent savedEvent = bankDepositEventRepository.save(event);

        List<User> matchedUsers = userRepository.findDepositNameMatches(normalizeName(request.depositorName()));
        if (couponProduct != null && matchedUsers.size() == 1) {
            chargeCoupon(savedEvent, matchedUsers.get(0), couponProduct, "BANK_TRANSFER_AUTO_MATCH");
        } else {
            savedEvent.markAdminRequired();
        }

        return new BankDepositWebhookResponse(savedEvent.getId(), savedEvent.getStatus().name(), false);
    }

    private void chargeCoupon(
            BankDepositEvent event,
            User user,
            CouponProduct couponProduct,
            String memo
    ) {
        event.markAutoMatched(user);
        user.increaseCouponCount(couponProduct.getCouponCount());

        adminCouponAdjustmentRepository.save(AdminCouponAdjustment.create(
                user,
                couponProduct.getCouponCount(),
                user.getRemainingCouponCount(),
                couponProduct.name(),
                event.getDepositorName(),
                event.getAmount(),
                memo
        ));

        event.markProcessed(user, couponProduct);
    }

    private CouponProduct resolveCouponProduct(Long amount) {
        return Arrays.stream(CouponProduct.values())
                .filter(product -> product.getPrice().equals(amount))
                .findFirst()
                .orElse(null);
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.replace(" ", "").toLowerCase();
    }

    private void validateWebhookSecret(String webhookSecret) {
        String expectedSecret = bankWebhookProperties.secret();
        if (expectedSecret == null || webhookSecret == null || !MessageDigest.isEqual(
                expectedSecret.getBytes(StandardCharsets.UTF_8),
                webhookSecret.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new GeneralException(ErrorStatus.UNAUTHORIZED);
        }
    }
}
