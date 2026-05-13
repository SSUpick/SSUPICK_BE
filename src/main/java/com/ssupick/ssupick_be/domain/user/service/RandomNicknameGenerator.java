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

    private static final int MAX_NICKNAME_LENGTH = 7;
    private static final int MAX_ATTEMPTS = 100;

    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private List<String> adjectives;
    private List<String> nouns;
    private List<String> nicknameCandidates;

    public RandomNicknameGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    void init() {
        adjectives = loadWords("nickname/adjectives.txt");
        nouns = loadWords("nickname/nouns.txt");
        nicknameCandidates = buildNicknameCandidates();
    }

    public String generate() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String nickname = nicknameCandidates.get(secureRandom.nextInt(nicknameCandidates.size()));

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

    private List<String> buildNicknameCandidates() {
        List<String> candidates = adjectives.stream()
                .flatMap(adjective -> nouns.stream()
                        .map(noun -> adjective + " " + noun))
                .filter(nickname -> nickname.length() <= MAX_NICKNAME_LENGTH)
                .distinct()
                .toList();

        if (candidates.isEmpty()) {
            throw new GeneralException(ErrorStatus.NICKNAME_GENERATION_FAILED);
        }

        return candidates;
    }
}
