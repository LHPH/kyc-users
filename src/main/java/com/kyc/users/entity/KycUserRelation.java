package com.kyc.users.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "KYC_USER_RELATION")
@Setter
@Getter
public class KycUserRelation implements Serializable {

    @Id
    private Long id;

    @MapsId
    @JoinColumn(name = "ID_USER",referencedColumnName = "ID")
    @OneToOne(fetch = FetchType.LAZY)
    private KycUser user;

    @Column(name = "ID_CUSTOMER")
    private Long idCustomer;

    @Column(name = "ID_EXECUTIVE")
    private Long idExecutive;

    @ManyToOne
    @JoinColumn(name = "USER_TYPE",referencedColumnName = "ID")
    private KycUserType userType;

}
