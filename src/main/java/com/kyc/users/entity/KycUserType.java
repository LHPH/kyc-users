package com.kyc.users.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "KYC_USER_TYPE")
@Data
public class KycUserType implements Serializable {

    @Id
    private Long id;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "userType",orphanRemoval = true)
    private List<KycUser> userRelations;

    @Column(name = "DESCRIPTION")
    private String description;
}
