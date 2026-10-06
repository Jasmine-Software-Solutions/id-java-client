package com.jasminesoftwaresolutions.idclient.dto.client;

import java.util.List;

/**
 * Mirrors {@code RestClientScopesController.ReplaceRequest} from id:interfaces.
 */
public class ReplaceScopesRequest {
    private List<ScopePayload> scopes;

    public ReplaceScopesRequest() {}

    public ReplaceScopesRequest(List<ScopePayload> scopes) {
        this.scopes = scopes;
    }

    public List<ScopePayload> getScopes() { return scopes; }
    public void setScopes(List<ScopePayload> scopes) { this.scopes = scopes; }
}
