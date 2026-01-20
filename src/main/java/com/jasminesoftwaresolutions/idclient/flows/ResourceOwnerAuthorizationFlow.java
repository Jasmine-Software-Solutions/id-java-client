package com.jasminesoftwaresolutions.idclient.flows;

import com.jasminesoftwaresolutions.idclient.IDClient;

import java.net.URI;
import java.net.URLEncoder;
import java.util.UUID;

public class ResourceOwnerAuthorizationFlow {
    public static URI getOAuth2AuthorizeUri(
            IDClient client,
            URI callbackUri,
            UUID tenant,
            String scope,
            String state) {
        URI endpointPath = client.getProvider().getEndpointPath("/oauth2/authorize");

        String queryString = "?";
        if (tenant != null)
            queryString += "tenant=" + tenant + "&";
        if (scope != null)
            queryString += "scope=" + scope + "&";
        if (state != null)
            queryString += "state=" + state + "&";

        String redirectUri = URLEncoder.encode(callbackUri.toString());
        queryString += "redirect_uri=" + redirectUri;
        queryString += "&response_type=code";
        queryString += "&client_id=" + client.getId();

        return URI.create(endpointPath + queryString);
    }

    public static URI getOAuth2TokenUri(
            IDClient client,
            URI callbackUri,
            String code) {
        URI endpointPath = client.getProvider().getEndpointPath("/api/v1/oauth2/token");

        String queryString = "?code=" + code;
        queryString += "&client_id=" + client.getId();

        String redirectUri = URLEncoder.encode(callbackUri.toString());
        queryString += "&redirect_uri=" + redirectUri;
        queryString += "&grant_type=authorization_code";

        return URI.create(endpointPath + queryString);
    }
}
