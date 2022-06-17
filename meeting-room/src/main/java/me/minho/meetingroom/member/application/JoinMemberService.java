package me.minho.meetingroom.member.application;

import me.minho.meetingroom.member.application.port.in.JoinMemberCommand;
import me.minho.meetingroom.member.application.port.in.JoinMemberException;
import me.minho.meetingroom.member.application.port.in.JoinMemberUseCase;
import me.minho.meetingroom.member.application.port.out.FindMemberPort;
import me.minho.meetingroom.member.application.port.out.SaveMemberPort;
import me.minho.meetingroom.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class JoinMemberService implements JoinMemberUseCase {

    private final FindMemberPort findMemberPort;

    private final SaveMemberPort saveMemberPort;

    JoinMemberService(FindMemberPort findMemberPort, SaveMemberPort saveMemberPort) {
        this.findMemberPort = findMemberPort;
        this.saveMemberPort = saveMemberPort;
    }

    @Override
    @Transactional
    public void joinMember(JoinMemberCommand joinMemberCommand) {
        Member member = joinMemberCommand.toMember();

        checkEmailDuplication(member);

        saveMemberPort.save(member);
    }

    private void checkEmailDuplication(Member member) {
        if (findMemberPort.findByEmail(member.getEmail()).isPresent()) {
            throw new JoinMemberException("이미 있는 존재하는 이메일");
        }
    }
}
