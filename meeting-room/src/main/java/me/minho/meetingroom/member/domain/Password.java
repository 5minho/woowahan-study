package me.minho.meetingroom.member.domain;

class Password {

    private static final int PASSWORD_MAX_LENGTH = 16;

    private static final int PASSWORD_MIN_LENGTH = 4;

    private final String value;

    Password(String value) {
        validatePassword(value);
        this.value = value;
    }

    private void validatePassword(String value) {
        if (value == null || value.length() < PASSWORD_MIN_LENGTH || value.length() > PASSWORD_MAX_LENGTH) {
            throw new MemberException("회원 비밀번호 유효성 검증 실패");
        }
    }

    String getValue() {
        return value;
    }
}
