package com.capstone.bobmate.controller;

import com.capstone.bobmate.dto.memberDto.JoinMemberDto;
import com.capstone.bobmate.repository.MemberRepository;
import com.capstone.bobmate.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @PostMapping("/signup")
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
    @PostMapping("/idDuplicate")
    public ResponseEntity<?> duplicateId(@RequestBody JoinMemberDto joinDto) {

        if (memberRepository.existsByLoginId(joinDto.getLoginId())) {
            log.warn("Id already exists {}", joinDto.getLoginId());
            return new ResponseEntity<>(true, HttpStatus.BAD_REQUEST);
        } else
            return new ResponseEntity<>(false, HttpStatus.OK);
    }


}