package com.capstone.bobmate.dto.partyDto;

import com.capstone.bobmate.domain.MatchingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PartyInfoDto {

    private String title;	// 파티 제목
    private int currentCount;	// 현재 인원 수
    private int maximumCount;	// 최대 인원 수
    private MatchingStatus status;	// 파티 매칭 상태

    private String name;	// 식당 이름

    private List<ResponsePartyMembersDto> responsePartyMembersDtoList;	// 파티원들 정보

    public PartyInfoDto(List<ResponsePartyMembersDto> responsePartyMembersDtoList){
        this.responsePartyMembersDtoList = responsePartyMembersDtoList;
    }
}
