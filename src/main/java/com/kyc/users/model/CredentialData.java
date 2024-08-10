package com.kyc.users.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("username='").append(username).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
