package com.kyc.users.repositories;

import com.kyc.users.entity.KycCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface KycCustomerRepository extends JpaRepository<KycCustomer,Long> {

    @Modifying
    @Query(name = "KycCustomer.setUserToCustomer")
    void setUserToCustomer(Long idUser,Long idCustomer);

    @Query(name = "KycCustomer.countHaveUser")
    long countHaveUser(Long idCustomer);
}
