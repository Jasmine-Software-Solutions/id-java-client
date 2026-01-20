package com.jasminesoftwaresolutions.idclient.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jasminesoftwaresolutions.idclient.IDClient;
import com.jasminesoftwaresolutions.idclient.IDResponses;
import com.jasminesoftwaresolutions.idclient.exceptions.UnexpectedResponseException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record Account(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Instant createdAt,
        boolean systemAdmin
) {
    public static CompletableFuture<Account> requestAccountDetailsForSession(IDClient client, Session session) {
        URI endpointUri = client.getProvider().getEndpointPath("/api/v1/account");

        HttpClient httpClient = client.getProvider().getHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(endpointUri)
                .header("Authorization", "Bearer " + session.accessToken())
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
                .thenApply(body -> {
                    UUID id = UUID.fromString(body.get("id").getAsString());

                    String firstName = IDResponses.getAsStringOrNull(body, "first_name");
                    String lastName = IDResponses.getAsStringOrNull(body, "last_name");
                    String email = IDResponses.getAsStringOrNull(body, "email");
                    Instant createdAt = IDResponses.getAsInstantOrNull(body, "created_at");
                    boolean systemAdmin = IDResponses.getAsBooleanOrFalse(body, "system_admin");

                    return new Account(id, firstName, lastName, email, createdAt, systemAdmin);
                });
    }

    public static CompletableFuture<Account> requestAccountDetailsWithSession(IDClient client, Session session, UUID id) {
        URI endpointUri = client.getProvider().getEndpointPath("/api/v1/accounts/" + id);

        HttpClient httpClient = client.getProvider().getHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(endpointUri)
                .header("Authorization", "Bearer " + session.accessToken())
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
                .thenApply(body -> {
                    String firstName = IDResponses.getAsStringOrNull(body, "first_name");
                    String lastName = IDResponses.getAsStringOrNull(body, "last_name");
                    String email = IDResponses.getAsStringOrNull(body, "email");
                    Instant createdAt = IDResponses.getAsInstantOrNull(body, "created_at");
                    boolean systemAdmin = IDResponses.getAsBooleanOrFalse(body, "system_admin");

                    return new Account(id, firstName, lastName, email, createdAt, systemAdmin);
                });
    }
}
