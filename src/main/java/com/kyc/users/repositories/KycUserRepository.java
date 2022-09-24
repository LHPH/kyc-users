package com.kyc.users.repositories;

import com.kyc.users.entity.KycUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KycUserRepository extends JpaRepository<KycUser, Long> {

    Optional<KycUser> findByUsername(String username);

    Optional<KycUser> findByUsernameAndActiveTrue(String username);
}
