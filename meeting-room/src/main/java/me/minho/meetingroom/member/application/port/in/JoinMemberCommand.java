package me.minho.meetingroom.member.application.port.in;

import me.minho.meetingroom.member.domain.Member;

public class JoinMemberCommand {

    private String email;

    private String password;

    public JoinMemberCommand(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Member toMember() {
        return Member.of(email, password);
    }
}
