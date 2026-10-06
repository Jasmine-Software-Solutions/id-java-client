package com.jasminesoftwaresolutions.idclient.dto.account;

import com.google.gson.annotations.SerializedName;

/**
 * Mirrors {@code RestAccountController.UpdateProfilePayload} from id:interfaces.
 */
public class UpdateProfilePayload {
    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    private String email;

    public UpdateProfilePayload() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
