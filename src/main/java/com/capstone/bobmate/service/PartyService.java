package com.capstone.bobmate.service;

import com.capstone.bobmate.domain.Member;
import com.capstone.bobmate.domain.Party;
import com.capstone.bobmate.domain.Restaurant;
import com.capstone.bobmate.dto.partyDto.PartyOwnerDto;
import com.capstone.bobmate.dto.partyDto.RequestPartyDto;
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
    public Boolean create(Member member, Restaurant restaurant, RequestPartyDto partyDto){

        // 현재 사용자
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);

        if (findMember.getIsJoined()){  // 파티에 이미 소속되어 있으면 파티 생성 불가
            return false;
        }

        // 파티 생성
        Party.create(findMember, restaurant, partyDto.getTitle(), partyDto.getMaximumCount());

        memberRepository.save(findMember);
        log.info("파티 생성 시 방장 여부: {}", findMember.getOwner());

        return true;
    }

    // 파티 리스트 보기
    public PartyOwnerDto parties(Long restaurantId, Member member){

        // 현재 사용자
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);

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

        // 사용자가 방장일 때
        if (findMember.getOwner()){
            Party membersParty = findMember.getParty();
            return new PartyOwnerDto(membersParty.getId(), responsePartyDto);
        }

        log.info("파티 목록: {}", responsePartyDto);

        return new PartyOwnerDto(responsePartyDto);
    }


    // 파티 참가
    @Transactional
    public List<ResponsePartyMembersDto> joinParty(Member member, Party party){

        // 현재 사용자
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);

        party.addMember(findMember);
        memberRepository.save(findMember);  // 멤버상태 업데이트

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

        // 현재 사용자
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);

        // 요청받은 partyId로 찾은 파티
        Party findParty = partyRepository.findById(partyId).orElseGet(null);

        log.info("방장 여부: {}", findMember.getOwner());
        log.info("요청 받은 party 와 member 가 속한 party 동일 여부: {}", findMember.getParty().getId() == findParty.getId());

        // 현재 파티에 속한 인원이 변경하려는 최대 인원수 보다 많은 경우 변경 실패
        if (findParty.getCurrentCount() > requestPartyDto.getMaximumCount()){
            return false;
        }

        // 사용자가 방장이고 && 자기 파티 수정을 요청했을 때
        if (findMember.getOwner() && (findMember.getParty().getId() == findParty.getId())){
            findParty.updateParty(requestPartyDto.getTitle(), requestPartyDto.getMaximumCount());
            partyRepository.save(findParty);

            return true;
        }
        // 방장이 아니거나 방장이지만 다른 파티 수정을 요청했을 때
        return false;

    }

    // 파티 탈퇴
    @Transactional
    public Boolean leaveParty(Member member, Long partyId){
        // 현재 사용자
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);

        // 사용자가 파티에 가입되어 있는지 확인
        if (findMember.getParty() == null){
            return false;
        }

        // 요청받은 partyId로 찾은 파티
        Party findParty = partyRepository.findById(partyId).orElseGet(null);
        log.info("요청 받은 party 와 member 가 속한 party 동일 여부: {}", findMember.getParty().getId() == findParty.getId());

        // member 의 partyId와 요청받은 party_id 가 같은지 확인
        if (findMember.getParty().getId() == findParty.getId()){
            // 사용자가 방장일 때 나가면 파티 삭제
            if (findMember.getOwner()){
                findMember.updateOwner(false);  // 방장 박탈
                log.info("방장 여부: {}", findMember.getOwner());

                // 참가한 유저들 강제로 파티 탈퇴
                List<Member> members = memberRepository.findByPartyId(findParty.getId());

                for (Member eachMember : members){
                    log.info("참가 멤버들: {}", eachMember.getNickname());
                    findParty.minusMember(eachMember);
                }
                memberRepository.save(findMember);
                partyRepository.deleteById(findParty.getId());

            } else {
                findParty.minusMember(findMember);
                memberRepository.save(findMember);
            }
            return true;
        }
        // 가입되어 있는 파티가 아닌 다른 파티 탈퇴를 요청했을 때
        return false;
    }


    // 파티 삭제
    public Boolean deleteParty(Member member, Long partyId){
        // 현재 사용자
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);

        // 사용자가 파티에 가입되어 있는지 확인
        if (findMember.getParty() == null){
            return false;
        }

        // 요청받은 partyId로 찾은 파티
        Party findParty = partyRepository.findById(partyId).orElseGet(null);
        log.info("요청 받은 party 와 member 가 속한 party 동일 여부: {}", findMember.getParty().getId() == findParty.getId());

        // member 의 partyId와 요청받은 party_id 가 같은지 && 사용자가 방장인지 확인
        if (findMember.getOwner() && (findMember.getParty().getId() == findParty.getId())){
            findMember.updateOwner(false);  // 방장 박탈
            log.info("방장 여부: {}", findMember.getOwner());

            // 참가한 유저들 강제로 파티 탈퇴
            List<Member> members = memberRepository.findByPartyId(findParty.getId());

            for (Member eachMember : members){
                log.info("파티 탈퇴될 멤버들: {}", eachMember.getNickname());
                findParty.minusMember(eachMember);
            }
            memberRepository.save(findMember);
            partyRepository.deleteById(findParty.getId());

            return true;
        }
        // 가입되어 있는 파티가 아닌 다른 파티 탈퇴를 요청했을 때
        return false;

    }


}
