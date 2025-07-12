package com.jasminesoftwaresolutions.idclient;

import com.google.gson.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Instant;

public class IDProvider {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toEpochMilli()))
            .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, typeOfT, context) -> Instant.ofEpochMilli(json.getAsLong()))
            .create();

    private final URI baseUrl;

    public IDProvider(URI baseUrl) {
        this.baseUrl = baseUrl;
    }

    public IDProvider(String baseUrl) {
        if (baseUrl.endsWith("/"))
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

        this.baseUrl = URI.create(baseUrl);
    }

    public URI getEndpointPath(String path) {
        if (!path.startsWith("/"))
            path = "/" + path;

        return URI.create(baseUrl.toString() + path);
    }

    public HttpClient getHttpClient() {
        return HTTP_CLIENT;
    }

    public Gson getGson() {
        return GSON;
    }
}
