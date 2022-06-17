package me.minho.meetingroom.member.domain;

public class Member {

    private final Email email;

    private final Password password;

    private Member(Email email, Password password) {
        this.email = email;
        this.password = password;
    }

    public static Member of(String email, String password) {
        return new Member(new Email(email), new Password(password));
    }

    public String getEmail() {
        return email.getValue();
    }

    public String getPassword() {
        return password.getValue();
    }
}
