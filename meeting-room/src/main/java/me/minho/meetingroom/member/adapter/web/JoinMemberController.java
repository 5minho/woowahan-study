package me.minho.meetingroom.member.adapter.web;

import me.minho.meetingroom.member.application.port.in.JoinMemberException;
import me.minho.meetingroom.member.application.port.in.JoinMemberUseCase;
import me.minho.meetingroom.member.domain.MemberException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class JoinMemberController {

    private final JoinMemberUseCase joinMemberUseCase;

    JoinMemberController(JoinMemberUseCase joinMemberUseCase) {
        this.joinMemberUseCase = joinMemberUseCase;
    }

    @PostMapping("/api/v1/join/members")
    String joinMember(@RequestBody JoinMemberRequest joinMemberRequest) {
        joinMemberUseCase.joinMember(joinMemberRequest.toCommand());

        return "OK";
    }

    @ExceptionHandler({MemberException.class, JoinMemberException.class})
    String handleJoinMemberException(Exception e) {
        return e.getMessage();
    }
}