package com.capstone.bobmate.dto.partyDto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PartyOwnerDto {

    private Long partyId;

    private List<ResponsePartyDto> responsePartyDtoList;

    public PartyOwnerDto(List<ResponsePartyDto> responsePartyDtoList){
        this.responsePartyDtoList = responsePartyDtoList;
    }
}
