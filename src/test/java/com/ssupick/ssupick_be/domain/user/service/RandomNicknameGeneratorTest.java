package com.ssupick.ssupick_be.domain.user.service;

import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RandomNicknameGeneratorTest {

    @Test
    void generate_returnsNicknameWithinSevenCharactersIncludingSpace() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.existsByNickname(anyString())).thenReturn(false);
        RandomNicknameGenerator generator = new RandomNicknameGenerator(userRepository);
        generator.init();

        for (int i = 0; i < 100; i++) {
            String nickname = generator.generate();

            assertThat(nickname).hasSizeLessThanOrEqualTo(7);
            assertThat(nickname).contains(" ");
        }
    }
}
