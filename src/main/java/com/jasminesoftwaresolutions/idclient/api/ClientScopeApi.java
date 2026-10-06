package com.jasminesoftwaresolutions.idclient.api;

import com.jasminesoftwaresolutions.idclient.IDClient;
import com.jasminesoftwaresolutions.idclient.core.UnexpectedResponseException;
import com.jasminesoftwaresolutions.idclient.dto.client.ClientScopesResponse;
import com.jasminesoftwaresolutions.idclient.dto.client.ReplaceScopesRequest;
import com.jasminesoftwaresolutions.idclient.retry.RetryableRequest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API client for client scope registration.
 * Supports both client-authenticated (Basic) and session-authenticated (Bearer) calls.
 */
public class ClientScopeApi {
    private final IDClient client;
    private final String sessionAccessToken;

    /**
     * Creates a scope API client using client credentials (Basic auth).
     * Only the client itself can register scopes via this mechanism.
     */
    public ClientScopeApi(IDClient client) {
        this.client = client;
        this.sessionAccessToken = null;
    }

    /**
     * Creates a scope API client using a session access token (Bearer auth).
     * This enables admin-scoped management of any client's scopes.
     */
    public ClientScopeApi(IDClient client, String sessionAccessToken) {
        this.client = client;
        this.sessionAccessToken = sessionAccessToken;
    }

    private String authHeader() {
        if (sessionAccessToken != null)
            return "Bearer " + sessionAccessToken;
        return client.getClientCredentials();
    }

    /**
     * Replaces all scopes for a client ({@code PUT /api/v1/clients/{clientId}/scopes}).
     */
    public CompletableFuture<ClientScopesResponse> replaceScopes(UUID clientId, ReplaceScopesRequest request) {
        URI uri = client.getProvider().getEndpointPath("/api/v1/clients/" + clientId + "/scopes");
        String body = client.getProvider().getGson().toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .uri(uri)
                .header("Authorization", authHeader())
                .header("Content-Type", "application/json")
                .build();

        return RetryableRequest.execute(
                () -> client.getProvider().getHttpClient().sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()),
                client.getRetryPolicy())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        return client.getProvider().getGson()
                                .fromJson(response.body(), ClientScopesResponse.class);
                    }
                    throw new UnexpectedResponseException(response.body(), "ClientScopesResponse", null);
                });
    }
}
