package com.jasminesoftwaresolutions.idclient.oauth2;

import com.jasminesoftwaresolutions.idclient.core.IDProvider;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Builds URIs for the OAuth2 Refresh Token flow.
 */
public final class RefreshTokenFlow {
    private RefreshTokenFlow() {}

    /**
     * Builds the token endpoint URI for refreshing an access token.
     *
     * @param provider     the ID provider configuration
     * @param refreshToken the refresh token to exchange
     */
    public static URI buildTokenUri(IDProvider provider, String refreshToken) {
        URI endpoint = provider.getEndpointPath("/api/v1/oauth2/token");

        String query = "?grant_type=refresh_token"
                + "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

        return URI.create(endpoint + query);
    }
}
