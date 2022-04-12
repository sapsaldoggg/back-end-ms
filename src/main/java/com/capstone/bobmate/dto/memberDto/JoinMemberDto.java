package com.capstone.bobmate.dto.memberDto;

import com.capstone.bobmate.domain.Sex;
import lombok.Data;

@Data
public class JoinMemberDto {

    private String loginId;    // 아이디
    private String nickname;    // 닉네임
    private String password;    // 비밀번호
    private String email;   // 이메일
    private Sex sex;  // 성별
    private String university;  // 대학교
    private String dept;    // 학과
    private int sno; // 학번

}
