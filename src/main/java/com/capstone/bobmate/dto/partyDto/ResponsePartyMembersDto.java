package com.capstone.bobmate.dto.partyDto;

import com.capstone.bobmate.domain.Sex;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponsePartyMembersDto {

    private String nickname;
    private String dept;
    private int sno;
    private Sex sex;
    private Long reliability;
    private Boolean owner;

}
