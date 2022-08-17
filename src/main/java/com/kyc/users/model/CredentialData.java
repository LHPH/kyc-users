package com.kyc.users.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CredentialData {

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9_]{6,10}$",message = "Bad format")
    private String username;
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9_#\\.\\+\\*\\$]{8,15}$",message = "Bad format")
    private String password;
}
