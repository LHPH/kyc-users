package com.kyc.users.repositories;

import com.kyc.users.entity.KycUserExtend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KycUserExtendRepository extends JpaRepository<KycUserExtend, Long> {

    Optional<KycUserExtend> findByUsername(String username);

    Optional<KycUserExtend> findByUsernameAndActiveTrue(String username);
}
