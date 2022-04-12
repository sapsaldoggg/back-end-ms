package com.capstone.bobmate.dto.restaurantDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseRestaurantDto {

    private Long id;    // 식당 id
    private String name;    // 식당 이름
    private String address; // 주소
    private double longitude;  // 경도
    private double latitude;    // 위도
    private String category;    // 카테고리

}
