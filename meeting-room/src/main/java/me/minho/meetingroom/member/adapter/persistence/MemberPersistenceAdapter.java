package me.minho.meetingroom.member.adapter.persistence;

import me.minho.meetingroom.member.application.port.out.FindMemberPort;
import me.minho.meetingroom.member.application.port.out.SaveMemberPort;
import me.minho.meetingroom.member.domain.Member;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class MemberPersistenceAdapter implements FindMemberPort, SaveMemberPort {

    private final MemberJpaRepository memberJpaRepository;

    MemberPersistenceAdapter(MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        MemberEntity memberEntity = memberJpaRepository.findByEmail(email);

        if (memberEntity == null) {
            return Optional.empty();
        }

        return Optional.of(Member.of(memberEntity.getEmail(), memberEntity.getPassword()));
    }

    @Override
    public void save(Member member) {
        memberJpaRepository.save(new MemberEntity(member.getEmail(), member.getPassword()));
    }

}
