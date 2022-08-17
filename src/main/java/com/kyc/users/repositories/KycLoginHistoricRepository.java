package com.kyc.users.repositories;


import com.kyc.users.entity.KycLoginHistoric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface KycLoginHistoricRepository extends JpaRepository<KycLoginHistoric,Long> {

    @Query(nativeQuery = true,name = "KycLoginHistoric.getCurrentSession")
    Optional<KycLoginHistoric> getCurrentSession(String sessionId);

    @Query(nativeQuery = true,name="KycLoginHistoric.getCurrentSessionOnChannel")
    Optional<KycLoginHistoric> getCurrentSessionOnChannel(Long idUser, Integer idChannel);
}
