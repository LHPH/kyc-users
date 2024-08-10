package com.kyc.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "KYC_CUSTOMER")
@Setter
@Getter
public class KycCustomer implements Serializable {

    @Id
    private Long id;

    @Column(name = "RFC")
    private String rfc;

    @Column(name = "ID_USER")
    private Long idUser;
}
