package com.jasminesoftwaresolutions.idclient.dto.client;

/**
 * Mirrors {@code RestClientAdminController.ResetSecretResponse} from id:interfaces.
 */
public class ResetSecretResponse {
    private String secret;

    public ResetSecretResponse() {}

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}
