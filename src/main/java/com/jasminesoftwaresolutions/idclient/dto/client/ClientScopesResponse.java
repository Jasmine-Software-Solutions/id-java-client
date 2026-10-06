package com.jasminesoftwaresolutions.idclient.dto.client;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.UUID;

/**
 * Mirrors the scope registration response from {@code PUT /api/v1/clients/{client_id}/scopes}.
 */
public class ClientScopesResponse {
    @SerializedName("client_id")
    private UUID clientId;

    private List<ScopeInfo> scopes;

    public ClientScopesResponse() {}

    public UUID getClientId() { return clientId; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }

    public List<ScopeInfo> getScopes() { return scopes; }
    public void setScopes(List<ScopeInfo> scopes) { this.scopes = scopes; }

    public static class ScopeInfo {
        private String id;
        private String description;

        public ScopeInfo() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
