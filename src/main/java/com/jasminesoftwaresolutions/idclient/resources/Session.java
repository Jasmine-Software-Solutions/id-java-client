package com.jasminesoftwaresolutions.idclient.resources;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jasminesoftwaresolutions.idclient.IDClient;
import com.jasminesoftwaresolutions.idclient.exceptions.UnexpectedResponseException;
import com.jasminesoftwaresolutions.idclient.flows.ResourceOwnerAuthorizationFlow;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

public record Session(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt
) {
    public boolean isAccessTokenExpired() {
        return accessTokenExpiresAt.isBefore(Instant.now());
    }

    public boolean isRefreshTokenExpired() {
        return refreshTokenExpiresAt.isBefore(Instant.now());
    }

    public static CompletableFuture<Session> requestWithAuthorizationCode(IDClient client, URI sourceUri, String code) {
        return client.requestApiToken().thenCompose(token -> {
            URI endpointUri = ResourceOwnerAuthorizationFlow.getOAuth2TokenUri(client, sourceUri, code);

            HttpClient httpClient = client.getProvider().getHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .GET().uri(endpointUri)
                    .header("Authorization", "Bearer " + token)
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(body -> {
                        try {
                            return JsonParser.parseString(body).getAsJsonObject();
                        } catch (JsonSyntaxException ex) {
                            throw new UnexpectedResponseException(body, "JSON Object", ex);
                        }
                    })
                    .thenApply(body -> new Session(
                            body.get("access_token").getAsString(),
                            body.get("refresh_token").getAsString(),
                            Instant.now().plusSeconds(body.get("expires_in").getAsLong()),
                            Instant.now().plusSeconds(body.get("refresh_token_expires_in").getAsLong())
                    ));
        });
    }

    public static CompletableFuture<Session> requestWithRefreshToken(IDClient client, String refreshToken) {
        return client.requestApiToken().thenCompose(token -> {
            URI endpointUri = client.getProvider().getEndpointPath("/oauth2/token");

            String queryString = "?grant_type=refresh_token&refresh_token=" + refreshToken;
            endpointUri = URI.create(endpointUri + queryString);

            HttpClient httpClient = client.getProvider().getHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .GET().uri(endpointUri)
                    .header("Authorization", "Bearer " + token)
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(body -> {
                        try {
                            return JsonParser.parseString(body).getAsJsonObject();
                        } catch (JsonSyntaxException ex) {
                            throw new UnexpectedResponseException(body, "JSON Object", ex);
                        }
                    })
                    .thenApply(body -> new Session(
                            body.get("access_token").getAsString(),
                            refreshToken,
                            Instant.now().plusSeconds(body.get("expires_in").getAsLong()),
                            Instant.now().plusSeconds(body.get("refresh_token_expires_in").getAsLong())
                    ));
        });
    }
}
