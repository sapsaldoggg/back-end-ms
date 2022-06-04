package com.capstone.bobmate.service;

import com.capstone.bobmate.domain.MatchingStatus;
import com.capstone.bobmate.domain.Member;
import com.capstone.bobmate.domain.Party;
import com.capstone.bobmate.domain.Restaurant;
import com.capstone.bobmate.dto.partyDto.*;
import com.capstone.bobmate.repository.MemberRepository;
import com.capstone.bobmate.repository.PartyRepository;
import com.capstone.bobmate.repository.RestaurantRepository;
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

    @Autowired
    private final RestaurantRepository restaurantRepository;

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
    public PartyInfoDto joinParty(Member member, Long partyId){
        // 현재 사용자
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);

        // 요청받은 partyId로 찾은 파티
        Party findParty = partyRepository.findById(partyId).orElseGet(null);

        // 파티가 가득 차있거나 || 사용자가 이미 파티에 속해있거나 || 파티 상태가 이미 MATCHED 일 때
        if ((findParty.getCurrentCount() == findParty.getMaximumCount()) || findMember.getIsJoined() || findParty.getStatus().equals(MatchingStatus.MATCHED))
            return null;

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

        Restaurant findRestaurant = findParty.getRestaurant();
        log.info("파티로 찾은 식당", findRestaurant);

        return new PartyInfoDto(findParty.getId(), findParty.getTitle(), findParty.getCurrentCount(), findParty.getMaximumCount(), findParty.getStatus(), findRestaurant.getName(), findParty.getCreatedAt(), responsePartyMembersDtos);
    }


    // 파티 수정
    @Transactional
    public Boolean updateParty(Member member, Long partyId, RequestPartyDto requestPartyDto){
        // 현재 사용자
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);

        // 요청받은 partyId로 찾은 파티
        Party findParty = partyRepository.findById(partyId).orElseGet(null);

        // 현재 파티에 속한 인원이 변경하려는 최대 인원수 보다 많은 경우 변경 실패
        if (findParty.getCurrentCount() > requestPartyDto.getMaximumCount())
            return false;

        // 사용자가 방장인 경우
        if (findMember.getOwner()){
            findParty.updateParty(requestPartyDto.getTitle(), requestPartyDto.getMaximumCount());
            return true;
        }
        // 이외의 경우
        return false;
    }


    // 파티 탈퇴 - MATCHED 상태에서 나가는 거 생각해야 함
    @Transactional
    public Boolean leaveParty(Member member, Long partyId){
        // 현재 사용자
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);

        // 사용자가 파티에 가입되어 있지 않은 경우
        if (findMember.getParty() == null)
            return false;

        // 요청받은 partyId로 찾은 파티
        Party findParty = partyRepository.findById(partyId).orElseGet(null);

        // 사용자가 방장일 때 나가면 파티 삭제
        if (findMember.getOwner()){
            findMember.updateOwner(false);  // 방장 박탈
            log.info("방장 박탈되었는지 여부: {}", findMember.getOwner());

            // 참가한 유저들 강제로 파티 탈퇴
            List<Member> members = memberRepository.findByPartyId(findParty.getId());

            for (Member eachMember : members){
                log.info("탈퇴될 멤버들: {}", eachMember.getNickname());
                findParty.minusMember(eachMember);
            }
            partyRepository.deleteById(findParty.getId());

            return true;
        } else if (!findMember.getOwner()) {    // 사용자가 파티원일 때
//            1) 매칭이 시작되면 탈퇴 불가
//            2) 매칭이 끝나면 탈퇴 가능
//            만약 방장이 까먹으면?
//                    =>
//            매칭이 시작된 시간을 DB에 저장
//            DB에 저장된 시간에서 5분이 지났을때부터 파티 탈퇴가 가능
            findParty.minusMember(findMember);
            return true;
        }
        // 이외의 경우
        return true;
    }


    // 파티 삭제
    @Transactional
    public Boolean deleteParty(Member member, Long partyId){
        // 현재 사용자
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);

        // 사용자가 파티에 가입되어 있지 않은 경우
        if (findMember.getParty() == null)
            return false;

        // 요청받은 partyId로 찾은 파티
        Party findParty = partyRepository.findById(partyId).orElseGet(null);

        // 사용자가 방장인 경우
        if (findMember.getOwner()){
            findMember.updateOwner(false);  // 방장 박탈
            log.info("방장 박탈되었는지 여부: {}", findMember.getOwner());

            // 참가한 유저들 강제로 파티 탈퇴
            List<Member> members = memberRepository.findByPartyId(findParty.getId());

            for (Member eachMember : members){
                log.info("파티 탈퇴될 멤버들: {}", eachMember.getNickname());
                findParty.minusMember(eachMember);
            }
            partyRepository.deleteById(findParty.getId());

            return true;
        }
        // 이외의 경우
        return false;
    }


    // 파티 준비 or 시작
    @Transactional
    public boolean readyParty(Member member, Long partyId){
        // 현재 사용자
        Member findMember = memberRepository.findById(member.getId()).orElseGet(null);

        // 요청받은 partyId로 찾은 파티
        Party findParty = partyRepository.findById(partyId).orElseGet(null);

        // 파티원들
        List<Member> members = memberRepository.findByPartyId(findParty.getId());

        // 방장 혼자 매치 시작 불가
        if (findMember.getOwner() && findParty.getCurrentCount() > 1) { // 방장인 경우 (방장이면 start 버튼)
            if (findMember.getIsReady()) { // MATCHED 상태에서 방장이 start 누르면 NON_MATCHED 로 상태 변경
                findMember.setReady(false);
                findParty.updateStatus(MatchingStatus.NON_MATCHED);
                log.info("매치가 취소되었습니다.");
                return true;
            }

            // NON_MATCHED 상태에서 방장이 start 누르는 경우
            if (!findParty.readyStatus(members))
                return false;

            findMember.setReady(true);
            findParty.updateStatus(MatchingStatus.MATCHED); // 매칭 완료
            log.info("매치가 시작되었습니다.");
            return true;

        // 사용자가 파티원인지 확인 (파티원이면 ready 버튼)
        } else if (!findMember.getOwner()){
            if (findParty.getStatus().equals(MatchingStatus.MATCHED)) // MATCHED 상태에선 준비 변경 불가
                return false;

            // ready 상태일 때 ready 취소
            if (findMember.getIsReady()) {
                findMember.setReady(false);
                log.info("준비가 취소되었습니다.");
            }
            else { // ready 상태일 때 ready 취소
                findMember.setReady(true);
                log.info("준비 되었습니다.");
            }
             return true;
        } else    // 이외의 경우 실패
            return false;
    }


}
