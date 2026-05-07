package com.ssupick.ssupick_be.domain.user.controller;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.common.status.SuccessStatus;
import com.ssupick.ssupick_be.domain.user.controller.docs.UserControllerDocs;
import com.ssupick.ssupick_be.domain.user.dto.request.RegisterUserOnboardingRequest;
import com.ssupick.ssupick_be.domain.user.dto.request.UpdateUserProfileRequest;
import com.ssupick.ssupick_be.domain.user.dto.response.GetTargetUserProfileResponse;
import com.ssupick.ssupick_be.domain.user.dto.response.GetUserCardResponse;
import com.ssupick.ssupick_be.domain.user.dto.response.GetUserProfileResponse;
import com.ssupick.ssupick_be.domain.user.dto.response.GetProfileViewListResponse;
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

    @PostMapping("/onboarding")
    @Override
    public ResponseEntity<ApiResponse<Void>> registerUserOnboarding(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid RegisterUserOnboardingRequest request
    ) {
        userService.registerUserOnboarding(userId, request);
        return ApiResponse.success(SuccessStatus.REGISTER_USER_ONBOARDING_SUCCESS);
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<List<GetUserCardResponse>>> getUserCardList(
            @AuthenticationPrincipal Long userId
    ) {
        List<GetUserCardResponse> response = userService.getUserCardList(userId);
        return ApiResponse.success(SuccessStatus.GET_USER_CARD_LIST_SUCCESS, response);
    }

    @GetMapping("/me")
    @Override
    public ResponseEntity<ApiResponse<GetUserProfileResponse>> getUserProfile(
            @AuthenticationPrincipal Long userId
    ) {
        GetUserProfileResponse response = userService.getUserProfile(userId);
        return ApiResponse.success(SuccessStatus.GET_USER_PROFILE_SUCCESS, response);
    }

    @GetMapping("/{targetUserId}")
    @Override
    public ResponseEntity<ApiResponse<GetTargetUserProfileResponse>> getTargetUserProfile(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long targetUserId
    ) {
        GetTargetUserProfileResponse response = userService.getTargetUserProfile(userId, targetUserId);
        return ApiResponse.success(SuccessStatus.GET_USER_TARGET_PROFILE_SUCCESS, response);
    }

    @PutMapping("/me")
    @Override
    public ResponseEntity<ApiResponse<Void>> updateUserProfile(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid UpdateUserProfileRequest request
    ) {
        userService.updateUserProfile(userId, request);
        return ApiResponse.success(SuccessStatus.UPDATE_USER_PROFILE_SUCCESS);
    }

    @GetMapping("/me/profile-views")
    @Override
    public ResponseEntity<ApiResponse<GetProfileViewListResponse>> getProfileViewList(
            @AuthenticationPrincipal Long userId
    ) {
        GetProfileViewListResponse response = userService.getProfileViewList(userId);
        return ApiResponse.success(SuccessStatus.GET_PROFILE_VIEW_LIST_SUCCESS, response);
    }
}
