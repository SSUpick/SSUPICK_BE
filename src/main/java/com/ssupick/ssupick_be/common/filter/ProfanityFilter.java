package com.ssupick.ssupick_be.common.filter;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.vane.badwordfiltering.BadWordFiltering;
import org.springframework.stereotype.Component;

import java.util.List;

// BadWordFiltering은 내부 가변 상태가 없어 thread-safe합니다. @Component 싱글톤으로 안전하게 공유됩니다.
@Component
public class ProfanityFilter {

    private final BadWordFiltering badWordFiltering = new BadWordFiltering();

    // 닉네임에 비속어 또는 띄어쓰기 우회 비속어가 포함되어 있으면 예외를 던집니다.
    public void validateNickname(String nickname) {
        if (containsProfanity(nickname)) {
            throw new GeneralException(ErrorStatus.NICKNAME_PROFANITY_DETECTED);
        }
    }

    // 어필 항목 목록에 비속어가 포함되어 있으면 예외를 던집니다.
    public void validateAppeals(List<String> appeals) {
        if (appeals == null) return;
        boolean hasProfanity = appeals.stream()
                .filter(appeal -> appeal != null && !appeal.isBlank())
                .anyMatch(this::containsProfanity);
        if (hasProfanity) {
            throw new GeneralException(ErrorStatus.APPEAL_PROFANITY_DETECTED);
        }
    }

    private boolean containsProfanity(String text) {
        return badWordFiltering.check(text) || badWordFiltering.blankCheck(text);
    }
}
