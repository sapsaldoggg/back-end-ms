package com.capstone.bobmate.service;

import com.capstone.bobmate.domain.Member;
import com.capstone.bobmate.domain.Party;
import com.capstone.bobmate.domain.Restaurant;
import com.capstone.bobmate.dto.partyDto.CreatePartyDto;
import com.capstone.bobmate.dto.partyDto.ResponsePartyDto;
import com.capstone.bobmate.dto.partyDto.ResponsePartyMembersDto;
import com.capstone.bobmate.repository.MemberRepository;
import com.capstone.bobmate.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PartyService {

    @Autowired
    private final MemberRepository memberRepository;

    @Autowired
    private final PartyRepository partyRepository;

    // 파티 생성
    @Transactional
    public void create(Member member, Restaurant restaurant, CreatePartyDto partyDto){
        Party.create(member, restaurant, partyDto.getTitle(), partyDto.getMaximumCount());

        memberRepository.save(member);
        log.info("파티 생성 시 방장 여부: {}", member.getOwner());
    }

    // 파티 리스트 보기
    public List<ResponsePartyDto> parties(Long restaurantId){

        List<Party> parties = partyRepository.findByRestaurantId(restaurantId);
        List<ResponsePartyDto> responsePartyDto = new ArrayList<>();

        for (Party party : parties){
            responsePartyDto.add(ResponsePartyDto.builder()
                    .id(party.getId())
                    .title(party.getTitle())
                    .status(party.getStatus())
                    .createdAt(party.getCreatedAt())
                    .currentCount(party.getCurrentCount())
                    .maximumCount(party.getMaximumCount())
                    .build()
            );

        }
        log.info("파티 목록: {}", responsePartyDto);

        return responsePartyDto;
    }


    // 파티 참가
    @Transactional
    public List<ResponsePartyMembersDto> joinParty(Member member, Party party){
        party.addMember(member);
        memberRepository.save(member);  // 멤버상태 업데이트

        List<Member> members = memberRepository.findByPartyId(party.getId());
        List<ResponsePartyMembersDto> responsePartyMembersDtos = new ArrayList<>();

        for (Member eachMember: members){
            responsePartyMembersDtos.add(ResponsePartyMembersDto.builder()
                    .nickname(eachMember.getNickname())
                    .dept(eachMember.getDept())
                    .sno(eachMember.getSno())
                    .sex(eachMember.getSex())
                    .reliability(eachMember.getReliability())
                    .owner(eachMember.getOwner())
                    .build()
            );
        }

        return responsePartyMembersDtos;
    }


}
