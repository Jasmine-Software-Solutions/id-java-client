package com.jasminesoftwaresolutions.idclient.oauth2;

import com.jasminesoftwaresolutions.idclient.core.IDProvider;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Builds URIs for the OAuth2 Authorization Code flow.
 */
public final class AuthorizationCodeFlow {
    private AuthorizationCodeFlow() {}

    public static URI buildAuthorizeUri(IDProvider provider, String clientFqdn, URI redirectUri,
                                        String scope, String state) {
        return buildAuthorizeUri(provider, UUID.nameUUIDFromBytes(clientFqdn.getBytes(StandardCharsets.UTF_8)),
                redirectUri, scope, state);
    }

    /**
     * Builds the authorization endpoint URI where the user should be redirected
     * to grant consent.
     *
     * @param provider    the ID provider configuration
     * @param clientId    the OAuth2 client ID
     * @param redirectUri the callback URI registered with the client
     * @param scope       space-delimited scope string (nullable)
     * @param state       opaque state value for CSRF protection (nullable)
     * @return the full authorization URI
     */
    public static URI buildAuthorizeUri(IDProvider provider, UUID clientId, URI redirectUri,
                                        String scope, String state) {
        URI endpoint = provider.getEndpointPath("/api/v1/oauth2/authorize");

        StringBuilder query = new StringBuilder("?");
        query.append("client_id=").append(clientId);
        query.append("&response_type=code");
        query.append("&redirect_uri=").append(URLEncoder.encode(redirectUri.toString(), StandardCharsets.UTF_8));

        if (scope != null && !scope.isBlank())
            query.append("&scope=").append(URLEncoder.encode(scope, StandardCharsets.UTF_8));

        if (state != null && !state.isBlank())
            query.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));

        return URI.create(endpoint + query.toString());
    }

    /**
     * Builds the token endpoint URI for exchanging an authorization code.
     *
     * @param provider    the ID provider configuration
     * @param clientId    the OAuth2 client ID
     * @param redirectUri the same redirect URI used in the authorize step
     * @param code        the authorization code received at the redirect URI
     * @return the token endpoint URI (GET request with Basic auth)
     */
    public static URI buildTokenUri(IDProvider provider, UUID clientId, URI redirectUri, String code) {
        URI endpoint = provider.getEndpointPath("/api/v1/oauth2/token");

        String query = "?grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri.toString(), StandardCharsets.UTF_8)
                + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8);

        return URI.create(endpoint + query);
    }
}
