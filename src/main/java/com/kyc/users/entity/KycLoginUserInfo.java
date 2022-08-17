package com.kyc.users.entity;

import com.kyc.core.exception.KycRestException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
    private KycUser user;

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
