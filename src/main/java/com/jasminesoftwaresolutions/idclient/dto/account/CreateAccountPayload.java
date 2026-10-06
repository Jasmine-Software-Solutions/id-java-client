package com.jasminesoftwaresolutions.idclient.dto.account;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Mirrors {@code RestAccountController.CreateAccountPayload} from id:interfaces.
 */
public class CreateAccountPayload {
    private String email;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    private String password;

    @SerializedName("invitation_id")
    private UUID invitationId;

    @SerializedName("invitation_token")
    private String invitationToken;

    public CreateAccountPayload() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UUID getInvitationId() { return invitationId; }
    public void setInvitationId(UUID invitationId) { this.invitationId = invitationId; }

    public String getInvitationToken() { return invitationToken; }
    public void setInvitationToken(String invitationToken) { this.invitationToken = invitationToken; }
}
