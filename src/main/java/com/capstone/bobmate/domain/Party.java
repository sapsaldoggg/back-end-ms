package com.capstone.bobmate.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@Entity
public class Party {

    @Id @GeneratedValue
    @Column(name = "party_id")
    private Long id;

    private String title;       // 파티 명

    @OneToMany(mappedBy = "party")
    private List<Member> members = new ArrayList<>();   // 파티에 속한 멤버들

    @OneToMany(mappedBy = "party")
    private List<ChatMessage> chatMessages = new ArrayList<>(); // 채팅방에 있는 채팅방

    @Enumerated(EnumType.STRING)
    private MatchingStatus status;      // 파티 매칭 상태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;  // 하나의 식당에 여러 파티가 있을 수 있음

    @CreatedDate
    private LocalDateTime createdAt;    // 파티 생성 시간

    private int currentCount;   // 현재 파티원 수

    private int maximumCount;   // 최대 파티원 수

    //== 생성 메서드 ==//
    public static Party create(Member member, Restaurant restaurant, String title, int maximumCount){
        Party party = new Party();
        party.members.add(member);
        party.setRestaurant(restaurant);
        party.setTitle(title);
        party.setMaximumCount(maximumCount);
        party.setStatus(MatchingStatus.NON_MATCHED);
        party.setCreatedAt(LocalDateTime.now());
        party.currentCount = party.getMembers().size();

        return party;
    }


    // 파티 현재 인원 조정
    public void setCurrentCount(Party party){
        this.currentCount = party.getMembers().size();
    }

}