package com.jasminesoftwaresolutions.idclient.api;

import com.google.gson.reflect.TypeToken;
import com.jasminesoftwaresolutions.idclient.IDClient;
import com.jasminesoftwaresolutions.idclient.core.UnexpectedResponseException;
import com.jasminesoftwaresolutions.idclient.dto.client.ClientResponse;
import com.jasminesoftwaresolutions.idclient.dto.client.CreateClientPayload;
import com.jasminesoftwaresolutions.idclient.dto.client.ResetSecretResponse;
import com.jasminesoftwaresolutions.idclient.dto.client.UpdateClientPayload;
import com.jasminesoftwaresolutions.idclient.retry.RetryableRequest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API client for OAuth2 client admin endpoints. Uses an access token (Bearer)
 * for session-based authorization.
 */
public class ClientAdminApi {
    private final IDClient client;
    private final String accessToken;

    public ClientAdminApi(IDClient client, String accessToken) {
        this.client = client;
        this.accessToken = accessToken;
    }

    private String authHeader() {
        return "Bearer " + accessToken;
    }

    /**
     * Lists all OAuth2 clients ({@code GET /api/v1/admin/clients}).
     */
    public CompletableFuture<List<ClientResponse>> listClients() {
        URI uri = client.getProvider().getEndpointPath("/api/v1/admin/clients");
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri)
                .header("Authorization", authHeader())
                .build();

        return execute(request, new TypeToken<List<ClientResponse>>() {});
    }

    /**
     * Gets a single client ({@code GET /api/v1/admin/clients/{clientId}}).
     */
    public CompletableFuture<ClientResponse> getClient(UUID clientId) {
        URI uri = client.getProvider().getEndpointPath("/api/v1/admin/clients/" + clientId);
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri)
                .header("Authorization", authHeader())
                .build();

        return execute(request, ClientResponse.class);
    }

    /**
     * Creates a new OAuth2 client ({@code POST /api/v1/admin/clients}).
     */
    public CompletableFuture<ClientResponse> createClient(CreateClientPayload payload) {
        URI uri = client.getProvider().getEndpointPath("/api/v1/admin/clients");
        String body = client.getProvider().getGson().toJson(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(uri)
                .header("Authorization", authHeader())
                .header("Content-Type", "application/json")
                .build();

        return execute(request, ClientResponse.class);
    }

    /**
     * Updates an OAuth2 client ({@code PUT /api/v1/admin/clients/{clientId}}).
     */
    public CompletableFuture<ClientResponse> updateClient(UUID clientId, UpdateClientPayload payload) {
        URI uri = client.getProvider().getEndpointPath("/api/v1/admin/clients/" + clientId);
        String body = client.getProvider().getGson().toJson(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .uri(uri)
                .header("Authorization", authHeader())
                .header("Content-Type", "application/json")
                .build();

        return execute(request, ClientResponse.class);
    }

    /**
     * Deletes an OAuth2 client ({@code DELETE /api/v1/admin/clients/{clientId}}).
     */
    public CompletableFuture<Void> deleteClient(UUID clientId) {
        URI uri = client.getProvider().getEndpointPath("/api/v1/admin/clients/" + clientId);
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE().uri(uri)
                .header("Authorization", authHeader())
                .build();

        return executeVoid(request);
    }

    /**
     * Resets a client's secret ({@code POST /api/v1/admin/clients/{clientId}/reset-secret}).
     * Returns the new plaintext secret.
     */
    public CompletableFuture<ResetSecretResponse> resetSecret(UUID clientId) {
        URI uri = client.getProvider().getEndpointPath("/api/v1/admin/clients/" + clientId + "/reset-secret");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(uri)
                .header("Authorization", authHeader())
                .build();

        return execute(request, ResetSecretResponse.class);
    }

    // ----- Internal -----

    private <T> CompletableFuture<T> execute(HttpRequest request, Class<T> responseType) {
        return RetryableRequest.execute(
                () -> client.getProvider().getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()),
                client.getRetryPolicy())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        return client.getProvider().getGson().fromJson(response.body(), responseType);
                    }
                    throw new UnexpectedResponseException(response.body(), responseType.getSimpleName(), null);
                });
    }

    private <T> CompletableFuture<T> execute(HttpRequest request, TypeToken<T> typeToken) {
        return RetryableRequest.execute(
                () -> client.getProvider().getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()),
                client.getRetryPolicy())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        return client.getProvider().getGson().fromJson(response.body(), typeToken.getType());
                    }
                    throw new UnexpectedResponseException(response.body(), typeToken.getType().getTypeName(), null);
                });
    }

    private CompletableFuture<Void> executeVoid(HttpRequest request) {
        return RetryableRequest.execute(
                () -> client.getProvider().getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()),
                client.getRetryPolicy())
                .thenApply(r -> null);
    }
}
