package com.jasminesoftwaresolutions.idclient.dto.client;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Mirrors {@code RestClientAdminController.CreateClientPayload} from id:interfaces.
 */
public class CreateClientPayload {
    private String name;
    private Boolean confidential;

    @SerializedName("redirect_uris")
    private List<String> redirectUris;

    public CreateClientPayload() {}

    public CreateClientPayload(String name, Boolean confidential, List<String> redirectUris) {
        this.name = name;
        this.confidential = confidential;
        this.redirectUris = redirectUris;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getConfidential() { return confidential; }
    public void setConfidential(Boolean confidential) { this.confidential = confidential; }

    public List<String> getRedirectUris() { return redirectUris; }
    public void setRedirectUris(List<String> redirectUris) { this.redirectUris = redirectUris; }
}
