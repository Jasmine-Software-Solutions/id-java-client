package com.jasminesoftwaresolutions.idclient.resources;

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
import java.util.concurrent.CompletableFuture;

public record Introspect(
        boolean active,
        String tokenType,
        String scope,
        String clientId,
        Instant exp,
        Instant iat
) {
    public static CompletableFuture<Introspect> introspectTokenWithToken(IDClient client, String token, String authenticatingToken) {
        URI endpointUri = client.getProvider().getEndpointPath("/oauth2/introspect?token=" + token);

        HttpClient httpClient = client.getProvider().getHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(endpointUri)
                .header("Authorization", "Bearer " + authenticatingToken)
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
                    boolean active = IDResponses.getAsBooleanOrFalse(body, "active");
                    String tokenType = IDResponses.getAsStringOrNull(body, "token_type");
                    String scope = IDResponses.getAsStringOrNull(body, "scope");
                    String clientId = IDResponses.getAsStringOrNull(body, "client_id");
                    Instant exp = IDResponses.getAsInstantOrNull(body, "exp");
                    Instant iat = IDResponses.getAsInstantOrNull(body, "iat");

                    return new Introspect(active, tokenType, scope, clientId, exp, iat);
                });
    }
} 