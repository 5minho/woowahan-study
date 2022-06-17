package me.minho.meetingroom.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @DisplayName("이메일값은 이메일 포맷이 이어야 한다.")
    static class ValidationTest {

        @DisplayName("실패 케이스")
        @ValueSource(strings = {"", "test", "test@", "test.com", "@"})
        @ParameterizedTest
        void createEmailFailTest(String invalidEmail) {
            Assertions.assertThatThrownBy(() -> new Email(invalidEmail))
                    .isInstanceOf(MemberException.class)
                    .hasMessage("회원 이메일 유효성 검증 실패");
        }

        @DisplayName("성공 케이스")
        @ValueSource(strings = {"test@test.com", "test@xyz.ab"})
        @ParameterizedTest
        void createEmailSuccessTest(String validEmail) {
            Email email = new Email(validEmail);

            Assertions.assertThat(email.getValue()).isEqualTo(validEmail);
        }
    }

}