package com.capstone.bobmate.service;

import com.capstone.bobmate.domain.Member;
import com.capstone.bobmate.domain.Party;
import com.capstone.bobmate.domain.Restaurant;
import com.capstone.bobmate.dto.partyDto.RequestPartyDto;
import com.capstone.bobmate.dto.partyDto.ResponsePartyDto;
import com.capstone.bobmate.dto.partyDto.ResponsePartyMembersDto;
import com.capstone.bobmate.repository.MemberRepository;
import com.capstone.bobmate.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    public void create(Member member, Restaurant restaurant, RequestPartyDto partyDto){
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

    // 파티 수정
    @Transactional
    public Boolean updateParty(Member member, Long partyId, RequestPartyDto requestPartyDto){

        // 요청받은 partyId로 찾은 파티
        Party findParty = partyRepository.findById(partyId).orElseGet(null);

        log.info("방장 여부: {}", member.getOwner());
        log.info("요청 받은 party 와 member 가 속한 party 동일 여부: {}", member.getParty().getId() == findParty.getId());

        // 현재 파티에 속한 인원이 변경하려는 최대 인원수 보다 많은 경우 변경 실패
        if (findParty.getCurrentCount() > requestPartyDto.getMaximumCount()){
            return false;
        }

        // 사용자가 방장이고 && 자기 파티 수정을 요청했을 때
        if (member.getOwner() && (member.getParty().getId() == findParty.getId())){
            findParty.updateParty(requestPartyDto.getTitle(), requestPartyDto.getMaximumCount());
            partyRepository.save(findParty);

            return true;
        }
        // 방장이 아니거나 방장이지만 다른 파티 수정을 요청했을 때
        return false;

    }


}
