package com.jasminesoftwaresolutions.idclient.oauth2;

import com.jasminesoftwaresolutions.idclient.core.IDProvider;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Builds URIs for the OAuth2 Client Credentials flow.
 */
public final class ClientCredentialsFlow {
    private ClientCredentialsFlow() {}

    /**
     * Builds the token endpoint URI for client credentials.
     *
     * @param provider the ID provider configuration
     * @param tenant   optional tenant to scope the token to
     * @param scope    space-delimited scope string (nullable)
     */
    public static URI buildTokenUri(IDProvider provider, UUID tenant, String scope) {
        URI endpoint = provider.getEndpointPath("/api/v1/oauth2/token");

        StringBuilder query = new StringBuilder("?grant_type=client_credentials");

        if (tenant != null)
            query.append("&tenant_id=").append(tenant);

        if (scope != null && !scope.isBlank())
            query.append("&scope=").append(URLEncoder.encode(scope, StandardCharsets.UTF_8));

        return URI.create(endpoint + query.toString());
    }
}
