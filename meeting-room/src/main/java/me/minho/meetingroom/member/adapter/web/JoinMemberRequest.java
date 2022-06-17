package me.minho.meetingroom.member.adapter.web;

import me.minho.meetingroom.member.application.port.in.JoinMemberCommand;

class JoinMemberRequest {

    private final String email;

    private final String password;

    public JoinMemberRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    JoinMemberCommand toCommand() {
        return new JoinMemberCommand(email, password);
    }
}
