package com.jasminesoftwaresolutions.idclient.api;

import com.google.gson.reflect.TypeToken;
import com.jasminesoftwaresolutions.idclient.IDClient;
import com.jasminesoftwaresolutions.idclient.core.UnexpectedResponseException;
import com.jasminesoftwaresolutions.idclient.dto.account.AccountResponse;
import com.jasminesoftwaresolutions.idclient.dto.account.CreateAccountPayload;
import com.jasminesoftwaresolutions.idclient.dto.account.UpdatePasswordPayload;
import com.jasminesoftwaresolutions.idclient.dto.account.UpdateProfilePayload;
import com.jasminesoftwaresolutions.idclient.retry.RetryableRequest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API client for account resources. Uses an access token (Bearer) for authorization.
 */
public class AccountApi {
    private final IDClient client;
    private final String accessToken;

    public AccountApi(IDClient client, String accessToken) {
        this.client = client;
        this.accessToken = accessToken;
    }

    private String authHeader() {
        return "Bearer " + accessToken;
    }

    // ----- Current account -----

    /**
     * Returns the currently authenticated account ({@code GET /api/v1/accounts/me}).
     */
    public CompletableFuture<AccountResponse> getMe() {
        URI uri = client.getProvider().getEndpointPath("/api/v1/accounts/me");
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri)
                .header("Authorization", authHeader())
                .build();

        return execute(request, AccountResponse.class);
    }

    // ----- Account CRUD -----

    /**
     * Returns an account by ID ({@code GET /api/v1/accounts/{id}}).
     */
    public CompletableFuture<AccountResponse> getAccount(UUID accountId) {
        URI uri = client.getProvider().getEndpointPath("/api/v1/accounts/" + accountId);
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri)
                .header("Authorization", authHeader())
                .build();

        return execute(request, AccountResponse.class);
    }

    /**
     * Creates a new account ({@code POST /api/v1/accounts}).
     */
    public CompletableFuture<AccountResponse> createAccount(CreateAccountPayload payload) {
        URI uri = client.getProvider().getEndpointPath("/api/v1/accounts");
        String body = client.getProvider().getGson().toJson(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(uri)
                .header("Authorization", authHeader())
                .header("Content-Type", "application/json")
                .build();

        return execute(request, AccountResponse.class);
    }

    /**
     * Lists all accounts ({@code GET /api/v1/entities/accounts}).
     */
    public CompletableFuture<List<EntitySummary>> listAccounts() {
        URI uri = client.getProvider().getEndpointPath("/api/v1/entities/accounts");
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri)
                .header("Authorization", authHeader())
                .build();

        return execute(request, new TypeToken<List<EntitySummary>>() {});
    }

    // ----- Profile -----

    /**
     * Updates an account's profile ({@code POST /api/v1/accounts/{id}/profile}).
     */
    public CompletableFuture<AccountResponse> updateProfile(UUID accountId, UpdateProfilePayload payload) {
        URI uri = client.getProvider().getEndpointPath("/api/v1/accounts/" + accountId + "/profile");
        String body = client.getProvider().getGson().toJson(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(uri)
                .header("Authorization", authHeader())
                .header("Content-Type", "application/json")
                .build();

        return execute(request, AccountResponse.class);
    }

    // ----- Password -----

    /**
     * Sets or updates an account's password ({@code POST /api/v1/accounts/{id}/password}).
     */
    public CompletableFuture<Void> setPassword(UUID accountId, String password) {
        URI uri = client.getProvider().getEndpointPath("/api/v1/accounts/" + accountId + "/password");
        String body = client.getProvider().getGson().toJson(new UpdatePasswordPayload(password));

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(uri)
                .header("Authorization", authHeader())
                .header("Content-Type", "application/json")
                .build();

        return executeVoid(request);
    }

    // ----- Account deletion -----

    /**
     * Deletes an account ({@code DELETE /api/v1/accounts/{id}}).
     */
    public CompletableFuture<Void> deleteAccount(UUID accountId) {
        URI uri = client.getProvider().getEndpointPath("/api/v1/accounts/" + accountId);
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE().uri(uri)
                .header("Authorization", authHeader())
                .build();

        return executeVoid(request);
    }

    // ----- Internal -----

    private <T> CompletableFuture<T> execute(HttpRequest request, Class<T> responseType) {
        return RetryableRequest.execute(
                () -> client.getProvider().getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()),
                client.getRetryPolicy())
                .thenApply(response -> parseBody(response, responseType));
    }

    private <T> CompletableFuture<T> execute(HttpRequest request, TypeToken<T> typeToken) {
        return RetryableRequest.execute(
                () -> client.getProvider().getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()),
                client.getRetryPolicy())
                .thenApply(response -> parseBody(response, typeToken));
    }

    private CompletableFuture<Void> executeVoid(HttpRequest request) {
        return RetryableRequest.execute(
                () -> client.getProvider().getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()),
                client.getRetryPolicy())
                .thenApply(r -> null);
    }

    private <T> T parseBody(HttpResponse<String> response, Class<T> type) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return client.getProvider().getGson().fromJson(response.body(), type);
        }
        throw new UnexpectedResponseException(response.body(), type.getSimpleName(), null);
    }

    private <T> T parseBody(HttpResponse<String> response, TypeToken<T> typeToken) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return client.getProvider().getGson().fromJson(response.body(), typeToken.getType());
        }
        throw new UnexpectedResponseException(response.body(), typeToken.getType().getTypeName(), null);
    }

    // ----- Entity summary -----

    public static class EntitySummary {
        private UUID id;
        private String name;

        public EntitySummary() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
