package com.capstone.bobmate.dto.partyDto;

import com.capstone.bobmate.domain.MatchingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PartyInfoDto {

    private long id;
    private String title;	// 파티 제목
    private int currentCount;	// 현재 인원 수
    private int maximumCount;	// 최대 인원 수
    private MatchingStatus status;	// 파티 매칭 상태
    private String restaurant;	// 식당 이름
    private LocalDateTime createdAt;    // 파티 생성 시간

    private List<ResponsePartyMembersDto> members;	// 파티원들 정보

    public PartyInfoDto(List<ResponsePartyMembersDto> responsePartyMembersDtoList){
        this.members = responsePartyMembersDtoList;
    }
}
