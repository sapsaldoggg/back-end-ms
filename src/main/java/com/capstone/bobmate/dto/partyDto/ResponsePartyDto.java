package com.capstone.bobmate.dto.partyDto;

import com.capstone.bobmate.domain.MatchingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResponsePartyDto {

    private Long id;
    private String title;   // 파티 명
    private MatchingStatus status;      // 파티 매칭 상태
    private LocalDateTime createdAt;    // 파티 생성 시간
    private int currentCount;   // 현재 파티원 수
    private int maximumCount;   // 최대 파티원 수

}
