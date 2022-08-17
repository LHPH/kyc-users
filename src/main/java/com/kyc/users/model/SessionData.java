package com.kyc.users.model;


import com.kyc.users.entity.KycUser;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Builder
@Getter
public class SessionData {

    private KycUser kycUser;
    private Integer idChannel;
    private String sessionId;
    private String ip;
    private Date newDate;
}
