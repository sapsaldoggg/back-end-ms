package com.capstone.bobmate.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Restaurant {

    @Id @GeneratedValue
    @Column(name = "restaurant_id")
    private Long id;

    private String name;    // 식당 이름

    private String address; // 주소

    private double longitude;  // 경도

    private double latitude;    // 위도

    private String category;    // 카테고리

    @OneToMany(mappedBy = "restaurant")
    private List<Party> parties = new ArrayList<>();    // 하나의 식당에는 여러 파티가 있을수 있음
}
