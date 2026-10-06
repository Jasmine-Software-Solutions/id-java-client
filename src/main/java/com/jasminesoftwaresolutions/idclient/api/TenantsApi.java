package com.jasminesoftwaresolutions.idclient.api;

import com.google.gson.reflect.TypeToken;
import com.jasminesoftwaresolutions.idclient.IDClient;
import com.jasminesoftwaresolutions.idclient.core.UnexpectedResponseException;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API client for tenant resources.
 * Authenticates via an access token (Bearer).
 */
public class TenantsApi {
    private final IDClient client;
    private final String accessToken;

    public TenantsApi(IDClient client, String accessToken) {
        this.client = client;
        this.accessToken = accessToken;
    }

    private String authHeader() {
        return "Bearer " + accessToken;
    }

    // ----- Tenants -----

    /**
     * Lists tenants the authenticated user can access
     * ({@code GET /api/v1/tenants}).
     */
    public CompletableFuture<List<TenantResponse>> listTenants() {
        URI uri = client.getProvider().getEndpointPath("/api/v1/tenants");
        System.out.println("[idclient:TenantsApi] listTenants: calling " + uri);
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri)
                .header("Authorization", authHeader())
                .build();

        return execute(request, new TypeToken<List<TenantResponse>>() {});
    }

    // ----- Response types -----

    public static class TenantResponse {
        private UUID id;
        private String name;
        private boolean suspended;

        public TenantResponse() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public boolean isSuspended() { return suspended; }
        public void setSuspended(boolean suspended) { this.suspended = suspended; }
    }

    // ----- Internal -----

    private <T> CompletableFuture<T> execute(HttpRequest request, TypeToken<T> typeToken) {
        return client.getProvider().getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("[idclient:TenantsApi] execute: status=" + response.statusCode() + " bodyLength=" + (response.body() != null ? response.body().length() : 0));
                    if (response.body() != null && response.body().length() < 500) {
                        System.out.println("[idclient:TenantsApi] execute: body=" + response.body());
                    }
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        T result = client.getProvider().getGson().fromJson(response.body(), typeToken.getType());
                        System.out.println("[idclient:TenantsApi] execute: parsed successfully, result=" + result);
                        return result;
                    }
                    System.out.println("[idclient:TenantsApi] execute: non-2xx, throwing UnexpectedResponseException");
                    throw new UnexpectedResponseException(response.body(), typeToken.getType().getTypeName(), null);
                });
    }
}
