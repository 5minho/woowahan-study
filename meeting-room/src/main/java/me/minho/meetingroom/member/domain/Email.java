package me.minho.meetingroom.member.domain;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$");

    private final String value;

    Email(String value) {
        validateEmail(value);
        this.value = value;
    }

    private void validateEmail(String value) {
        Matcher matcher = EMAIL_PATTERN.matcher(value);

        if (!matcher.find()) {
            throw new MemberException("회원 이메일 유효성 검증 실패");
        }
    }

    String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
