package com.capstone.bobmate.controller;

import com.capstone.bobmate.config.auth.PrincipalDetails;
import com.capstone.bobmate.domain.Member;
import com.capstone.bobmate.dto.memberDto.JoinMemberDto;
import com.capstone.bobmate.repository.MemberRepository;
import com.capstone.bobmate.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class HomeController {

    @Autowired
    private final MemberService memberService;

    @Autowired
    private final MemberRepository memberRepository;


    // 회원 가입
    @PostMapping("/join")
    public ResponseEntity<?> registerUser(@RequestBody JoinMemberDto joinDto){
        try{
//            if (joinDto == null || joinDto.getLoginId().equals(null)){
//                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//            }
            if (memberRepository.existsByLoginId(joinDto.getLoginId())) {
                log.warn("Id already exists {}", joinDto.getLoginId());
                return new ResponseEntity<>(true, HttpStatus.BAD_REQUEST);
            }
            memberService.create(joinDto);
            return new ResponseEntity<>("ok", HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 회원가입 때 아이디 중복 검사
    @PostMapping("/duplicate-loginId")
    public ResponseEntity<?> duplicateId(@RequestBody JoinMemberDto joinDto) {

        if (memberRepository.existsByLoginId(joinDto.getLoginId())) {
            log.warn("Id already exists {}", joinDto.getLoginId());
            return new ResponseEntity<>(true, HttpStatus.BAD_REQUEST);
        } else
            return new ResponseEntity<>(false, HttpStatus.OK);
    }

    // 회원가입 때 닉네임 중복 검사
    @PostMapping("/duplicate-nickname")
    public ResponseEntity<?> duplicateNickName(@RequestBody JoinMemberDto joinDto) {

        if (memberRepository.existsByNickname(joinDto.getNickname())) {
            log.warn("Nickname already exists {}", joinDto.getNickname());
            return new ResponseEntity<>(true, HttpStatus.BAD_REQUEST);
        } else
            return new ResponseEntity<>(false, HttpStatus.OK);
    }

    @GetMapping("/user")
    public void detail(@AuthenticationPrincipal PrincipalDetails principalDetails){
        Member member = principalDetails.getMember();
        log.info("member: {}", member.getNickname());
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);
        log.info("nfindMember: {}", findMember.getNickname());
    }


}
