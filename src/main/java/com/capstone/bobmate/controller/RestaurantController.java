package com.capstone.bobmate.controller;

import com.capstone.bobmate.dto.restaurantDto.ResponseRestaurantDto;
import com.capstone.bobmate.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class RestaurantController {

    private final RestaurantService restaurantService;

    // 식당 목록 리턴
    @GetMapping("/restaurants")
    public ResponseEntity<?> restaurantsList(){
        try{
            List<ResponseRestaurantDto> restaurantDtos = restaurantService.restaurants();
            return new ResponseEntity<>(restaurantDtos, HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }



}
