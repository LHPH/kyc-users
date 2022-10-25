package com.kyc.users.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "KYC_PARAMETERS")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycParameter {

    @Id
    @Column(name = "PARAM_KEY")
    private String key;

    @Column(name = "PARAM_vALUE")
    private String value;
}
