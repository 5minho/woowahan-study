package me.minho.meetingroom.member.application.port.out;

import me.minho.meetingroom.member.domain.Member;

import java.util.Optional;

public interface FindMemberPort {
    Optional<Member> findByEmail(String emailValue);
}
