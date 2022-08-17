package com.kyc.users.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "KYC_USER")
@Entity
@Data
@NoArgsConstructor
public class KycUser implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @Column(name ="SECRET")
    private String secret;

    @Column(name ="ACTIVE")
    private Boolean active;

    @Column(name ="LOCKED")
    private Boolean locked;

    @Column(name ="DATE_CREATION")
    private Date dateCreation;

    @Column(name = "DATE_UPDATED")
    private Date dateUpdated;

    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private KycLoginUserInfo loginUserInfo;

    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL)
    private KycUserRelation userRelation;


}
