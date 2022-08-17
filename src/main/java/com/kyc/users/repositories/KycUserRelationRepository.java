package com.kyc.users.repositories;

import com.kyc.users.entity.KycUserRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KycUserRelationRepository extends JpaRepository<KycUserRelation,Long> {

    Optional<KycUserRelation> findByIdCustomer(Long idCustomer);

    Optional<KycUserRelation> findByIdExecutive(Long idExecutive);
}
