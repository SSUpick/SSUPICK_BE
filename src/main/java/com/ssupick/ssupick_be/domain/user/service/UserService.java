package com.ssupick.ssupick_be.domain.user.service;

import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 카카오 유저 조회 후 없으면 신규 생성, 탈퇴 유저면 복구
    @Transactional
    public User findOrRegisterKakaoUser(
            String kakaoId, String email, String name, String profileUrl, DeviceType deviceType
    ) {
        return userRepository.findByOauthIdAndOauthProvider(kakaoId, OAuthProvider.KAKAO)
                .map(user -> {
                    if (user.isDeleted())
                        user.restore(email, name, profileUrl, deviceType);
                    return user;
                })
                .orElseGet(() ->
                        userRepository.save(User.createKakaoUser(kakaoId, email, name, profileUrl, deviceType))
                );
    }

}
