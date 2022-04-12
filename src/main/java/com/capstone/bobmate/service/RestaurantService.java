package com.capstone.bobmate.service;

import com.capstone.bobmate.domain.Restaurant;
import com.capstone.bobmate.dto.restaurantDto.ResponseRestaurantDto;
import com.capstone.bobmate.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public List<ResponseRestaurantDto> restaurants(){
        List<Restaurant> restaurants = restaurantRepository.findAll();
        List<ResponseRestaurantDto> responseRestaurantDtos = new ArrayList<>();

        for (Restaurant restaurant: restaurants){
            responseRestaurantDtos.add(ResponseRestaurantDto.builder()
                    .id(restaurant.getId())
                    .name(restaurant.getName())
                    .address(restaurant.getAddress())
                    .longitude(restaurant.getLongitude())
                    .latitude(restaurant.getLatitude())
                    .category(restaurant.getCategory())
                    .build()
            );
        }

        log.info("식당 목록: {}", responseRestaurantDtos);

        return responseRestaurantDtos;
    }


}
