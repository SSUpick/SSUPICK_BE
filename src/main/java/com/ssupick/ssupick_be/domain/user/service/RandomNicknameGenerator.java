package com.ssupick.ssupick_be.domain.user.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;

@Component
public class RandomNicknameGenerator {

    private static final int MAX_NICKNAME_LENGTH = 10;
    private static final int MAX_ATTEMPTS = 100;

    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private List<String> adjectives;
    private List<String> nouns;

    public RandomNicknameGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    void init() {
        adjectives = loadWords("nickname/adjectives.txt");
        nouns = loadWords("nickname/nouns.txt");
    }

    public String generate() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String nickname = adjectives.get(secureRandom.nextInt(adjectives.size()))
                    + " "
                    + nouns.get(secureRandom.nextInt(nouns.size()));

            if (isAvailable(nickname)) {
                return nickname;
            }
        }

        throw new GeneralException(ErrorStatus.NICKNAME_GENERATION_FAILED);
    }

    private List<String> loadWords(String path) {
        ClassPathResource resource = new ClassPathResource(path);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> words = reader.lines()
                    .map(String::trim)
                    .filter(word -> !word.isBlank())
                    .distinct()
                    .toList();

            if (words.isEmpty()) {
                throw new GeneralException(ErrorStatus.NICKNAME_GENERATION_FAILED);
            }

            return words;
        } catch (IOException e) {
            throw new GeneralException(ErrorStatus.NICKNAME_GENERATION_FAILED);
        }
    }

    private boolean isAvailable(String nickname) {
        return nickname.length() <= MAX_NICKNAME_LENGTH
                && !userRepository.existsByNickname(nickname);
    }
}
