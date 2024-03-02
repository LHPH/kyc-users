package com.kyc.users.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
