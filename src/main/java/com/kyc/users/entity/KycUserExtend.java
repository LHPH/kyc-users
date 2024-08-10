package com.kyc.users.entity;

import com.kyc.core.persistence.entity.base.BaseKycUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

;


@Table(name = "KYC_USER")
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class KycUserExtend extends BaseKycUser implements Serializable {

    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private KycLoginUserInfo loginUserInfo;
}
