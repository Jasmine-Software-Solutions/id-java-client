package com.jasminesoftwaresolutions.idclient;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jasminesoftwaresolutions.idclient.exceptions.UnexpectedResponseException;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IDClient {
    private final IDProvider provider;

    private final UUID id;
    private final String secret;

    public IDClient(IDProvider provider, UUID id, String secret) {
        this.provider = provider;
        this.id = id;
        this.secret = secret;
    }

    public String getClientCredentials() {
        String decodedCredentials = id + ":" + secret;
        return "Basic " + Base64.getEncoder().encodeToString(decodedCredentials.getBytes());
    }

    public CompletableFuture<String> requestApiToken() {
        HttpClient client = provider.getHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(provider.getEndpointPath("/oauth2/token?grant_type=client_credentials"))
                .header("Authorization", getClientCredentials())
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    try {
                        return JsonParser.parseString(body).getAsJsonObject();
                    } catch (JsonSyntaxException ex) {
                        throw new UnexpectedResponseException(body, "JSON Object", ex);
                    }
                })
                .thenApply(body -> body.get("access_token").getAsString());
    }

    public IDProvider getProvider() {
        return provider;
    }

    public UUID getId() {
        return id;
    }
}
