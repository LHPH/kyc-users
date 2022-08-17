package com.kyc.users.repositories;

import com.kyc.users.entity.KycUserType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycUserTypeRepository extends JpaRepository<KycUserType,Long> {
}
