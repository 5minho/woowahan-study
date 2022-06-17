package me.minho.meetingroom.member.adapter.persistence;

import me.minho.meetingroom.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {

    MemberEntity findByEmail(String email);

}
