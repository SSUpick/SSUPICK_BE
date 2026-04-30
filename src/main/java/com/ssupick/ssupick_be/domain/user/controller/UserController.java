package com.ssupick.ssupick_be.domain.user.controller;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.common.status.SuccessStatus;
import com.ssupick.ssupick_be.domain.user.controller.docs.UserControllerDocs;
import com.ssupick.ssupick_be.domain.user.dto.request.UserOnboardingRequest;
import com.ssupick.ssupick_be.domain.user.dto.response.UserCardResponse;
import com.ssupick.ssupick_be.domain.user.dto.response.UserProfileResponse;
import com.ssupick.ssupick_be.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private final UserService userService;

    @GetMapping("/me")
    @Override
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.success(SuccessStatus.GET_USER_PROFILE_SUCCESS,
                userService.getUserProfile(userId));
    }

    @PostMapping("/onboarding")
    @Override
    public ResponseEntity<ApiResponse<Void>> completeOnboarding(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid UserOnboardingRequest request
    ) {
        userService.completeOnboarding(userId, request);
        return ApiResponse.success(SuccessStatus.COMPLETE_ONBOARDING_SUCCESS);
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<List<UserCardResponse>>> getUserCardList(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.success(SuccessStatus.GET_USER_CARD_LIST_SUCCESS,
                userService.getUserCardList(userId));
    }
}
