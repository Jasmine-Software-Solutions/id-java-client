package com.jasminesoftwaresolutions.idclient.dto.client;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.UUID;

/**
 * Mirrors {@code RestClientAdminController.ClientResponse} from id:interfaces.
 */
public class ClientResponse {
    private UUID id;
    private String name;
    private boolean confidential;
    private boolean suspended;

    @SerializedName("redirect_uris")
    private List<String> redirectUris;

    @SerializedName("created_at")
    private long createdAt;

    public ClientResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isConfidential() { return confidential; }
    public void setConfidential(boolean confidential) { this.confidential = confidential; }

    public boolean isSuspended() { return suspended; }
    public void setSuspended(boolean suspended) { this.suspended = suspended; }

    public List<String> getRedirectUris() { return redirectUris; }
    public void setRedirectUris(List<String> redirectUris) { this.redirectUris = redirectUris; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
