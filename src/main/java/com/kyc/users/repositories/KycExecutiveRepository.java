package com.kyc.users.repositories;

import com.kyc.users.entity.KycExecutive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycExecutiveRepository extends JpaRepository<KycExecutive,Long> {

    KycExecutive findByIdUser(Long idUser);
}
