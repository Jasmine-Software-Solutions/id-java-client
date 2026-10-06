package com.jasminesoftwaresolutions.idclient.dto.client;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Mirrors {@code RestClientAdminController.UpdateClientPayload} from id:interfaces.
 */
public class UpdateClientPayload {
    private String name;
    private Boolean confidential;
    private Boolean suspended;

    @SerializedName("redirect_uris")
    private List<String> redirectUris;

    public UpdateClientPayload() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getConfidential() { return confidential; }
    public void setConfidential(Boolean confidential) { this.confidential = confidential; }

    public Boolean getSuspended() { return suspended; }
    public void setSuspended(Boolean suspended) { this.suspended = suspended; }

    public List<String> getRedirectUris() { return redirectUris; }
    public void setRedirectUris(List<String> redirectUris) { this.redirectUris = redirectUris; }
}
