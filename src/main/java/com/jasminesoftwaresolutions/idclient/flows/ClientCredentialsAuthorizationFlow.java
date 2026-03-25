package com.jasminesoftwaresolutions.idclient.flows;

import com.jasminesoftwaresolutions.idclient.IDClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public class ClientCredentialsAuthorizationFlow {
    public static URI getOAuth2TokenUri(IDClient client, UUID tenant, String scope) {
        Objects.requireNonNull(tenant, "tenant is required for client_credentials");

        URI endpointPath = client.getProvider().getEndpointPath("/api/v1/oauth2/token");

        String queryString = "?grant_type=client_credentials";
        queryString += "&tenant=" + tenant;
        if (scope != null && !scope.isBlank())
            queryString += "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);

        return URI.create(endpointPath + queryString);
    }
}
