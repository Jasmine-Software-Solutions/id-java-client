package com.jasminesoftwaresolutions.idclient.dto.account;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Mirrors {@code RestAccountController.SsoClientResponse} from id:interfaces —
 * one SSO application the principal may launch.
 *
 * The wire is Gson under {@code LOWER_CASE_WITH_UNDERSCORES}, and
 * {@code IDProvider}'s Gson carries no field-naming policy, so every
 * multi-word field needs its {@link SerializedName}. Without it Gson would
 * look for {@code clientId} and leave the field null with no error.
 */
public class SsoClientResponse {
    @SerializedName("client_id")
    private UUID clientId;

    private String fqdn;

    private String name;

    @SerializedName("logo_uri")
    private String logoUri;

    @SerializedName("initiate_login_uri")
    private String initiateLoginUri;

    public SsoClientResponse() {}

    public UUID getClientId() { return clientId; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }

    public String getFqdn() { return fqdn; }
    public void setFqdn(String fqdn) { this.fqdn = fqdn; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogoUri() { return logoUri; }
    public void setLogoUri(String logoUri) { this.logoUri = logoUri; }

    public String getInitiateLoginUri() { return initiateLoginUri; }
    public void setInitiateLoginUri(String initiateLoginUri) { this.initiateLoginUri = initiateLoginUri; }
}
