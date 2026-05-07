package com.ssupick.ssupick_be.domain.user.dto.request;

import java.util.List;

public record UpdateUserProfileCommand(
        String nickname,
        String mbti,
        List<String> appeals,
        String contact
) {}
