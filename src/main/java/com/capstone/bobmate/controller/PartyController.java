package com.capstone.bobmate.controller;

import com.capstone.bobmate.config.auth.PrincipalDetails;
import com.capstone.bobmate.domain.Member;
import com.capstone.bobmate.domain.Party;
import com.capstone.bobmate.domain.Restaurant;
import com.capstone.bobmate.dto.partyDto.PartyInfoDto;
import com.capstone.bobmate.dto.partyDto.RequestPartyDto;
import com.capstone.bobmate.dto.partyDto.PartyOwnerDto;
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

            Boolean create = partyService.create(member, restaurant, partyDto);

            if (create){    // 정상적으로 생성
                return new ResponseEntity<>(true, HttpStatus.OK);
            } else {    // 비정상적으로 생성
                return new ResponseEntity<>(false, HttpStatus.OK);
            }

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
            PartyOwnerDto partyOwnerDto = partyService.parties(restaurantId, member);

            return new ResponseEntity<>(partyOwnerDto, HttpStatus.OK);

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

            PartyInfoDto partyInfoDto = partyService.joinParty(member, partyId);
//            List<ResponsePartyMembersDto> responsePartyMembersDtos = partyService.joinParty(member, partyId);

            if (partyInfoDto.getResponsePartyMembersDtoList() == null){  // 파티 참가에 문제가 발생
                return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(partyInfoDto, HttpStatus.OK);

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


    // 파티 탈퇴
    @PostMapping("/party/{party_id}/out")
    public ResponseEntity<?> partyOut(
            @PathVariable(name = "party_id") Long partyId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        try{
            Member member = principalDetails.getMember();
            log.info("현재 로그인 한 사용자: {}", member.getNickname());

            Boolean quit = partyService.leaveParty(member, partyId);

            if (quit){ // 탈퇴가 잘 수행됨
                return new ResponseEntity<>(true, HttpStatus.OK);
            } else {    // 탈퇴에 문제 발생
                return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }


    // 파티 삭제
    @DeleteMapping("/party/{party_id}")
    public ResponseEntity<?> partyDelete(
            @PathVariable(name = "party_id") Long partyId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        try {
            Member member = principalDetails.getMember();
            log.info("현재 로그인 한 사용자: {}", member.getNickname());

            Boolean delete = partyService.deleteParty(member, partyId);

            if (delete) {   // 삭제가 잘 수행됨
                return new ResponseEntity<>(true, HttpStatus.OK);
            } else {    // 삭제에 문제 발생
                return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    // 준비 or 시작 버튼 클릭
    @PostMapping("/party/{party_id}/ready")
    public ResponseEntity<?> partyReady(
            @PathVariable(name = "party_id") Long partyId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        try{
            Member member = principalDetails.getMember();
            log.info("현재 로그인 한 사용자: {}", member.getNickname());

            int ready = partyService.readyParty(member, partyId);

            if (ready == 0) {    // 준비 or 시작이 잘 눌림
                return new ResponseEntity<>(true, HttpStatus.OK);
            } else if (ready == 1){ // 모든 파티원이 ready 상태가 아닌데 방장이 start 한 경우
                return new ResponseEntity<>(false, HttpStatus.OK);
            } else {    // 준비 or 시작에 문제 발생
                return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }


    }


}
