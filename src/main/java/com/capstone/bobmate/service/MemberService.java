package com.capstone.bobmate.service;

import com.capstone.bobmate.domain.Member;
import com.capstone.bobmate.domain.RoleType;
import com.capstone.bobmate.dto.memberDto.JoinMemberDto;
import com.capstone.bobmate.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    @Autowired
    private final MemberRepository memberRepository;

    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Member create(JoinMemberDto joinDto){

        Member member = Member.builder()
                .loginId(joinDto.getLoginId())
                .nickname(joinDto.getNickname())
                .password(bCryptPasswordEncoder.encode(joinDto.getPassword()))
                .email(joinDto.getEmail())
                .sex(joinDto.getSex())
                .university(joinDto.getUniversity())
                .dept(joinDto.getDept())
                .sno(joinDto.getSno())
                .reliability(0L)
                .owner(false)
                .isJoined(false)
                .role(RoleType.ROLE_USER)
                .build();

        return memberRepository.save(member);
    }




}
