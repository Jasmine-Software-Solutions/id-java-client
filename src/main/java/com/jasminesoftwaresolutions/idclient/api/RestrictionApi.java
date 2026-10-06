package com.jasminesoftwaresolutions.idclient.api;

import com.google.gson.reflect.TypeToken;
import com.jasminesoftwaresolutions.idclient.IDClient;
import com.jasminesoftwaresolutions.idclient.core.UnexpectedResponseException;
import com.jasminesoftwaresolutions.idclient.dto.restriction.PlaceRestrictionRequest;
import com.jasminesoftwaresolutions.idclient.dto.restriction.RestrictionResponse;
import com.jasminesoftwaresolutions.idclient.retry.RetryableRequest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API client for security restrictions.
 * Authenticates via an access token (Bearer), consistent with
 * {@link AccountApi} and {@link ClientAdminApi}.
 */
public class RestrictionApi {
    private final IDClient client;
    private final String accessToken;

    public RestrictionApi(IDClient client, String accessToken) {
        this.client = client;
        this.accessToken = accessToken;
    }

    private String authHeader() {
        return "Bearer " + accessToken;
    }

    // ----- Place -----

    /**
     * Places a restriction ({@code POST /api/v1/restrictions}).
     *
     * @param type            restriction type: {@code "account_lock"} or {@code "ip_block"}
     * @param targetType      target category: {@code "account"} or {@code "ip"}
     * @param targetId        UUID string (for accounts) or IP string (for IP blocks)
     * @param eventId         the security event that triggered this restriction
     * @param durationMinutes optional duration in minutes; null means permanent
     * @return the placed restriction
     */
    public CompletableFuture<RestrictionResponse> placeRestriction(
            String type, String targetType, String targetId,
            UUID eventId, Integer durationMinutes) {
        return placeRestriction(new PlaceRestrictionRequest(
                type, targetType, targetId, eventId, durationMinutes));
    }

    /**
     * Places a restriction ({@code POST /api/v1/restrictions}).
     */
    public CompletableFuture<RestrictionResponse> placeRestriction(PlaceRestrictionRequest payload) {
        URI uri = client.getProvider().getEndpointPath("/api/v1/restrictions");
        String body = client.getProvider().getGson().toJson(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(uri)
                .header("Authorization", authHeader())
                .header("Content-Type", "application/json")
                .build();

        return execute(request, RestrictionResponse.class);
    }

    // ----- List -----

    /**
     * Lists active restrictions, optionally filtered by target
     * ({@code GET /api/v1/restrictions?target_type=&target_id=}).
     */
    public CompletableFuture<List<RestrictionResponse>> listRestrictions(
            String targetType, String targetId) {
        StringBuilder query = new StringBuilder("?");
        if (targetType != null && !targetType.isBlank())
            query.append("target_type=").append(targetType).append("&");
        if (targetId != null && !targetId.isBlank())
            query.append("target_id=").append(targetId).append("&");

        URI uri = client.getProvider().getEndpointPath("/api/v1/restrictions" + query);

        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri)
                .header("Authorization", authHeader())
                .build();

        return execute(request, new TypeToken<List<RestrictionResponse>>() {});
    }

    /**
     * Lists all active restrictions ({@code GET /api/v1/restrictions}).
     */
    public CompletableFuture<List<RestrictionResponse>> listRestrictions() {
        return listRestrictions(null, null);
    }

    // ----- Lift -----

    /**
     * Lifts (deletes) a restriction ({@code DELETE /api/v1/restrictions/{id}}).
     */
    public CompletableFuture<Void> liftRestriction(UUID restrictionId) {
        URI uri = client.getProvider().getEndpointPath("/api/v1/restrictions/" + restrictionId);

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
