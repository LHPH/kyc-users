package com.kyc.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Table(name = "KYC_LOGIN_HISTORIC")
@Entity
@Data
public class KycLoginHistoric implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER",referencedColumnName = "ID")
    private KycUserExtend user;

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

    @Column(name = "DATE_CHECKPOINT")
    private Date dateCheckpoint;

    @Column(name = "DATE_LOGOUT")
    private Date dateLogout;
}
