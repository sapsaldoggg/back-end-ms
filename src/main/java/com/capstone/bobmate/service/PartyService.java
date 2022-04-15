package com.capstone.bobmate.service;

import com.capstone.bobmate.domain.MatchingStatus;
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
    public List<ResponsePartyMembersDto> joinParty(Member member, Long partyId){
        // 현재 사용자
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);

        // 요청받은 partyId로 찾은 파티
        Party findParty = partyRepository.findById(partyId).orElseGet(null);

        // 파티가 가득 차있거나 || 사용자가 이미 파티에 속해있거나 || 파티 상태가 이미 MATCHED 일 때
        if ((findParty.getCurrentCount() == findParty.getMaximumCount()) || findMember.getIsJoined() || findParty.getStatus().equals(MatchingStatus.MATCHED)){
            return null;
        }

        findParty.addMember(findMember);
        memberRepository.save(findMember);  // 멤버상태 업데이트

        List<Member> members = memberRepository.findByPartyId(findParty.getId());
        List<ResponsePartyMembersDto> responsePartyMembersDtos = new ArrayList<>();

        for (Member eachMember: members){
            responsePartyMembersDtos.add(ResponsePartyMembersDto.builder()
                    .nickname(eachMember.getNickname())
                    .dept(eachMember.getDept())
                    .sno(eachMember.getSno())
                    .sex(eachMember.getSex())
                    .reliability(eachMember.getReliability())
                    .owner(eachMember.getOwner())
                    .ready(eachMember.getIsReady())
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

        // 사용자가 방장인지 && member 의 partyId와 요청받은 party_id 가 같은지 확인
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


    // 파티 준비 or 시작  - 이후 코드 변경
    public int readyParty(Member member, Long partyId){
        // 현재 사용자
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);

        // 요청받은 partyId로 찾은 파티
        Party findParty = partyRepository.findById(partyId).orElseGet(null);
        log.info("요청 받은 party 와 member 가 속한 party 동일 여부: {}", findMember.getParty().getId() == findParty.getId());

        // 파티원들
        List<Member> members = memberRepository.findByPartyId(findParty.getId());

        // 사용자가 파티에 가입되어 있는지 확인
        if (findMember.getParty() == null){
            return 2;
        }

        if (findParty.getCurrentCount() == 1){  // 파티원이 방장밖에 없을 때 start 불가
            return 1;
        }

        // member 의 partyId와 요청받은 party_id 가 같은지 && 사용자가 방장인지 확인 (방장이면 start 버튼)
        if ((findMember.getParty().getId() == findParty.getId() && findMember.getOwner())){
            // 매칭 상태에서 방장이 start 다시 누르면 NON_MATCHED 로 상태 변경
            if (findParty.getStatus().equals(MatchingStatus.MATCHED)){
                findParty.updateStatus(MatchingStatus.NON_MATCHED);
                findMember.setReady(false);
                memberRepository.save(findMember);
                partyRepository.save(findParty);
                return 0;
            }
            findMember.setReady(true);
            for (Member eachMember : members){  // 모든 파티원의 ready 상태 확인
                log.info("참가 멤버들: {}", eachMember.getNickname());
                log.info("참가 멤버들의 ready 상태: {}", eachMember.getIsReady());
                if (!eachMember.getIsReady()){  // ready 안한 파티원이 있는 경우
                    findMember.setReady(false);
                    return 1;
                }
            }
            findParty.updateStatus(MatchingStatus.MATCHED); // 매칭 완료
            memberRepository.save(findMember);
            partyRepository.save(findParty);
            return 0;
        } else if ((findMember.getParty().getId() == findParty.getId() && !findMember.getOwner())){  // 사용자가 파티원인지 확인 (파티원이면 ready 버튼)
            findMember.setReady(true);
            memberRepository.save(findMember);
            return 0;
        } else {    // 이외의 경우 실패
            return 2;
        }
    }


}
