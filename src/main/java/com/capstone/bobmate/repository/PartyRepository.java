package com.capstone.bobmate.repository;

import com.capstone.bobmate.domain.Party;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyRepository extends JpaRepository<Party, Long> {
    List<Party> findByRestaurantId(Long restaurantId);
}
