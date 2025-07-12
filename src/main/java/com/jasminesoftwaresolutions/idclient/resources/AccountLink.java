package com.jasminesoftwaresolutions.idclient.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jasminesoftwaresolutions.idclient.IDClient;
import com.jasminesoftwaresolutions.idclient.exceptions.UnexpectedResponseException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record AccountLink(
        UUID accountId,
        UUID tenantId,
        boolean administrator
) {
    public static CompletableFuture<Set<AccountLink>> requestForSession(IDClient client, Session session) {
        URI endpointUri = client.getProvider().getEndpointPath("/api/v1/account/links");

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
                    Set<AccountLink> accountLinks = new HashSet<>();

                    Set<String> tenantIds = body.keySet();
                    for (String tenantId : tenantIds) {
                        JsonObject link = body.getAsJsonObject(tenantId);
                        boolean administrator = link.get("administrator").getAsBoolean();

                        accountLinks.add(new AccountLink(
                                null,
                                UUID.fromString(tenantId),
                                administrator
                        ));
                    }

                    return accountLinks;
                });
    }
}
