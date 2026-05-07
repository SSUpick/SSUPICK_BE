package com.ssupick.ssupick_be.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "열람 목록 통합 응답")
public class GetProfileViewListResponse {

    @Schema(description = "내가 열람한 유저 목록")
    private List<GetUserViewResponse> viewedUsers;

    @Schema(description = "나를 열람한 유저 목록")
    private List<GetUserViewResponse> viewerUsers;

    public static GetProfileViewListResponse of(
            List<GetUserViewResponse> viewedUsers,
            List<GetUserViewResponse> viewerUsers
    ) {
        return GetProfileViewListResponse.builder()
                .viewedUsers(viewedUsers)
                .viewerUsers(viewerUsers)
                .build();
    }
}
