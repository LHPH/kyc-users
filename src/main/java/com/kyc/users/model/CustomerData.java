package com.kyc.users.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Setter
@Getter
public class CustomerData extends CredentialData{

    @NotNull
    private Long customerNumber;

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("{");
        sb.append("customer=").append(customerNumber);
        sb.append('}');
        return sb.toString();
    }

    public CustomerData(String username, String password, Long customerNumber){
        super(username,password);
        this.customerNumber = customerNumber;
    }
}
