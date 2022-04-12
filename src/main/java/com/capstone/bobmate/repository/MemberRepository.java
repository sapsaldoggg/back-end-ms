package com.capstone.bobmate.repository;

import com.capstone.bobmate.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByLoginId(String loginId);
    Boolean existsByLoginId(String loginId);

    List<Member> findByPartyId(Long partyId);
}