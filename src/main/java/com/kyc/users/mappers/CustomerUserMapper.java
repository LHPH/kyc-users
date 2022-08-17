package com.kyc.users.mappers;

import com.kyc.core.annotations.EncodedMapping;
import com.kyc.core.services.PasswordEncoderService;
import com.kyc.users.entity.KycUser;
import com.kyc.users.enums.KycUserTypeEnum;
import com.kyc.users.model.CustomerData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;


@Mapper(componentModel = "spring",uses = {PasswordEncoderService.class})
public interface CustomerUserMapper {


    @Mappings({
            @Mapping(target="username", source="user.username"),
            @Mapping(target="secret", source="user.password",qualifiedBy = EncodedMapping.class),
            @Mapping(target="active", constant = "true"),
            @Mapping(target="locked", constant = "false"),
            @Mapping(target="dateCreation",expression = "java(new java.util.Date())")
    })
    KycUser toEntityForSigningUp(CustomerData user);

    @Named("getUserType")
    static Long getUserType(){
        return KycUserTypeEnum.CUSTOMER.getId();
    }
}
