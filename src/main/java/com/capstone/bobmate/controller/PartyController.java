package com.capstone.bobmate.controller;

import com.capstone.bobmate.config.auth.PrincipalDetails;
import com.capstone.bobmate.domain.Member;
import com.capstone.bobmate.domain.Party;
import com.capstone.bobmate.domain.Restaurant;
import com.capstone.bobmate.dto.partyDto.CreatePartyDto;
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
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody CreatePartyDto partyDto,
            @PathVariable(name = "restaurant_id") Long restaurantId){
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


    // 파티 리스트 보기
    @GetMapping("/{restaurant_id}/parties")
    public ResponseEntity<?> showParties(@PathVariable(name = "restaurant_id") Long restaurantId){
        try{
            List<ResponsePartyDto> partyDtos = partyService.parties(restaurantId);
            return new ResponseEntity<>(partyDtos, HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    // 파티 참가하기
    @PostMapping("/party/join/{party_id}")
    public ResponseEntity<?> memberJoinParty(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                       @PathVariable(name = "party_id") Long partyId){
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


        // 1. 파티를 찾고
        // 2. 파티에 member 를 추가해주고 (member 테이블의 party_id 에 값이 들어가야 함)
        // 3. 파티를 저장하고
        // 4. 파티에 있는 members 리스트 정보를 모두 리턴
    }


}
