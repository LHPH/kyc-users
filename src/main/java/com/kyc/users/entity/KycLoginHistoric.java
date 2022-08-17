package com.kyc.users.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "KYC_LOGIN_HISTORIC")
@Entity
@Data
public class KycLoginHistoric implements Serializable {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER",referencedColumnName = "ID")
    private KycUser user;

    @Column(name = "IP")
    private String ip;

    @Column(name = "ID_CHANNEL")
    private Integer idChannel;

    @Column(name = "ID_SESSION")
    private String idSession;

    @Column(name = "ACTIVE_SESSION")
    private Boolean activeSession;

    @Column(name = "DATE_LOGIN")
    private Date dateLogin;

    @Column(name = "DATE_LOGOUT")
    private Date dateLogout;
}
