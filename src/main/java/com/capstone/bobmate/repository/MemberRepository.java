package com.capstone.bobmate.repository;

import com.capstone.bobmate.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByLoginId(String loginId);
    Boolean existsByLoginId(String loginId);
    Boolean existsByNickname(String nickname);
    List<Member> findByPartyId(Long partyId);
}
