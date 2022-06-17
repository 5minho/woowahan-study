package me.minho.meetingroom.member.application.port.out;

import me.minho.meetingroom.member.domain.Member;

public interface SaveMemberPort {
    void save(Member member);
}
