package com.jasminesoftwaresolutions.idclient.dto.account;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Mirrors {@code RestAccountController.AccountResponse} from id:interfaces.
 */
public class AccountResponse {
    private UUID id;
    private String email;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    public AccountResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
