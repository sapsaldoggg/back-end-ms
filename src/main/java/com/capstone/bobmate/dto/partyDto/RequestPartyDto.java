package com.capstone.bobmate.dto.partyDto;

import lombok.Data;


@Data
public class RequestPartyDto {

    private String title;   // 파티 명
    private int maximumCount;   // 최대 파티원 수

}
