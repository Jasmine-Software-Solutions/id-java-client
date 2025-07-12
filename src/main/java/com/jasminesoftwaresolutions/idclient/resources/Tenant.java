package com.jasminesoftwaresolutions.idclient.resources;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jasminesoftwaresolutions.idclient.IDClient;
import com.jasminesoftwaresolutions.idclient.exceptions.UnexpectedResponseException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record Tenant(UUID id, String name, Instant createdAt) {
    public static CompletableFuture<Tenant> requestForSession(IDClient client, Session session) {
        URI endpointUri = client.getProvider().getEndpointPath("/api/v1/tenant");

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
                    String name = body.get("name").getAsString();
                    Instant createdAt = Instant.ofEpochMilli(body.get("created_at").getAsLong());

                    return new Tenant(id, name, createdAt);
                });
    }
}