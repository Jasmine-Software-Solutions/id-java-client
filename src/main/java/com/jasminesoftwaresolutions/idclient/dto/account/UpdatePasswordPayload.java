package com.jasminesoftwaresolutions.idclient.dto.account;

/**
 * Mirrors {@code RestAccountController.UpdatePasswordPayload} from id:interfaces.
 */
public class UpdatePasswordPayload {
    private String password;

    public UpdatePasswordPayload() {}

    public UpdatePasswordPayload(String password) {
        this.password = password;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
