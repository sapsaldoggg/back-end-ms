package com.capstone.bobmate.controller;

import com.capstone.bobmate.config.auth.PrincipalDetails;
import com.capstone.bobmate.domain.Member;
import com.capstone.bobmate.domain.Party;
import com.capstone.bobmate.domain.Restaurant;
import com.capstone.bobmate.dto.partyDto.RequestPartyDto;
import com.capstone.bobmate.dto.partyDto.PartyOwnerDto;
import com.capstone.bobmate.dto.partyDto.ResponsePartyDto;
import com.capstone.bobmate.dto.partyDto.ResponsePartyMembersDto;
import com.capstone.bobmate.repository.PartyRepository;
import com.capstone.bobmate.repository.RestaurantRepository;
import com.capstone.bobmate.service.PartyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user/restaurant")
public class PartyController {

    @Autowired
    private final PartyService partyService;

    @Autowired
    private final PartyRepository partyRepository;

    @Autowired
    private final RestaurantRepository restaurantRepository;

    // 파티 생성
    @PostMapping("/{restaurant_id}/party")
    public ResponseEntity<?> createParty(
            @PathVariable(name = "restaurant_id") Long restaurantId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody RequestPartyDto partyDto){

        try{
            Member member = principalDetails.getMember();
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseGet(null);

            if (member.getIsJoined() == true){  // 파티에 이미 소속되어 있으면 파티 생성 불가
                return new ResponseEntity<>("already joined", HttpStatus.BAD_REQUEST);
            }

            partyService.create(member, restaurant, partyDto);
            return new ResponseEntity<>(true, HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    // 식당 선택 시 파티 목록 조회
    @GetMapping("/{restaurant_id}/parties")
    public ResponseEntity<?> showParties(
            @PathVariable(name = "restaurant_id") Long restaurantId,
            @AuthenticationPrincipal PrincipalDetails principalDetails){

        try{
            // 방장 여부 확인을 위해 멤버 조회
            Member member = principalDetails.getMember();
            log.info("현재 로그인 한 사용자: {}", member.getNickname());

            // 식당 id로 찾은 파티 목록
            List<ResponsePartyDto> partyDtos = partyService.parties(restaurantId);

            // 사용자가 방장일 때
            if (member.getOwner()){
                Party membersParty = member.getParty();
                return new ResponseEntity<>(new PartyOwnerDto(membersParty.getId(), partyDtos), HttpStatus.OK);
            }
            return new ResponseEntity<>(new PartyOwnerDto(partyDtos), HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    // 파티 참가
    @PostMapping("/party/{party_id}")
    public ResponseEntity<?> memberJoinParty(
            @PathVariable(name = "party_id") Long partyId,
            @AuthenticationPrincipal PrincipalDetails principalDetails){

        try{
            Member member = principalDetails.getMember();
            log.info("현재 로그인 한 사용자: {}", member.getNickname());

            Party party = partyRepository.findById(partyId).orElseGet(null);
            log.info("찾은 파티: {}", party);

            if (party.getCurrentCount() == party.getMaximumCount()){    // 파티가 가득 차 있음
                return new ResponseEntity<>("full", HttpStatus.BAD_REQUEST);
            } else if(member.getIsJoined().equals(true)){   // 사용자가 파티에 이미 속해있음
                return new ResponseEntity<>("already joined", HttpStatus.BAD_REQUEST);
            }
            List<ResponsePartyMembersDto> responsePartyMembersDtos = partyService.joinParty(member, party);

            return new ResponseEntity<>(responsePartyMembersDtos, HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 파티 수정
    @PutMapping("/party/{party_id}")
    public ResponseEntity<?> partyUpdate(
            @PathVariable(name = "party_id") Long partyId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody RequestPartyDto requestPartyDto){

        try{
            Member member = principalDetails.getMember();
            log.info("현재 로그인 한 사용자: {}", member.getNickname());

            Boolean isChanged = partyService.updateParty(member, partyId, requestPartyDto);

            if (isChanged){ // 변경이 잘 수행됨
                return new ResponseEntity<>(true, HttpStatus.OK);
            } else {    // 변경에 문제 발생
                return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


}
