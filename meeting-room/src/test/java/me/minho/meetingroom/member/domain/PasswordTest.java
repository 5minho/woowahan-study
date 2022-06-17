package me.minho.meetingroom.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordTest {

    @DisplayName("비밀번호는 4자 이상 16자 이하의 문자열로 생성 가능하다.")
    static class ValidationTest {

        @DisplayName("생성 실패 케이스")
        @ValueSource(strings = {"", "123", "12345678901234567"})
        @ParameterizedTest
        void createPasswordFailTest(String invalidPassword) {
            assertThatThrownBy(() -> new Password(invalidPassword))
                    .isInstanceOf(MemberException.class)
                    .hasMessage("회원 비밀번호 유효성 검증 실패");
        }


        @DisplayName("생성 성공 케이스")
        @ValueSource(strings = {"1234", "1234567890123456"})
        @ParameterizedTest
        void createPasswordSuccessTest(String validPassword) {
            Password password = new Password(validPassword);

            assertThat(password.getValue()).isEqualTo(validPassword);
        }

    }

}