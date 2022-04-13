package com.capstone.bobmate.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String loginId;      // 아이디

    private String nickname;    // 닉네임

    private String password;    // 비밀번호

    private String email;       // 이메일

    @Enumerated(EnumType.STRING)
    private Sex sex;      // 성별

    private String university;  // 대학교

    private String dept;        // 학과

    private int sno;            // 학번

    private Long reliability;   // 신뢰도

    private Boolean owner;     // 방장 여부

    private Boolean isJoined;   // 이미 다른 파티에 속해있는지 여부

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @OneToMany(mappedBy = "sender")
    private List<ChatMessage> chatMessages = new ArrayList<>();    // 한 유저는 여러 채팅 메시지를 칠 수 있음

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "party_id")
    private Party party;        // 한 파티에는 여러명이 들어갈 수 있음


    // 방장 여부 변경
    public void updateOwner(Boolean owner){
        this.owner = owner;
    }

    // 참가 여부 변경
    public void setJoined(Boolean joined){
        this.isJoined = joined;
    }

    // 파티 참가
    public void setParty(Party party){
        this.party = party;
    }


}
