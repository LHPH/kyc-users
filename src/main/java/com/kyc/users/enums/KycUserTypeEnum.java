package com.kyc.users.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum KycUserTypeEnum {

    UNKNOWN(0L),CUSTOMER(1L),EXECUTIVE(2L),SYSTEM(3L);

    private Long id;

    public static KycUserTypeEnum getInstanceById(Long id){

        KycUserTypeEnum result = UNKNOWN;

        for(KycUserTypeEnum value : KycUserTypeEnum.values()){

            if(value.getId().equals(id)){

                result = value;
                break;
            }
        }
        return result;

    }
}
