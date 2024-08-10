package com.kyc.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "KYC_LOGIN_USER_INFO")
@Setter
@Getter
public class KycLoginUserInfo implements Serializable {

    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER",referencedColumnName = "ID")
    private KycUserExtend user;

    @Column(name = "DATE_FIRST_LOGIN")
    private Date dateFirstLogin;

    @Column(name = "DATE_LAST_SUCCESSFUL_LOGIN")
    private Date dateLastSuccessfulLogin;

    @Column(name = "DATE_LAST_FAILURE_LOGIN")
    private Date dateLastFailureLogin;

    @Column(name = "DATE_LOCKED_USER")
    private Date dateLockedUser;

    @Column(name="FAIL_ATTEMPTS_CURRENT_LOGIN")
    private Integer numFailAttemptsCurrentLogin;
}
