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

    private final URI frontendUrl;
    private final URI apiUrl;

    public IDProvider(URI frontendUrl, URI apiUrl) {
        this.frontendUrl = frontendUrl;
        this.apiUrl = apiUrl;
    }

    public IDProvider(String frontendUrl, String apiUrl) {
        this.frontendUrl = normalizeUri(frontendUrl);
        this.apiUrl = normalizeUri(apiUrl);
    }

    public URI getEndpointPath(String path) {
        if (!path.startsWith("/"))
            path = "/" + path;

        if (!path.startsWith("/api"))
            return URI.create(frontendUrl.toString() + path);

        return URI.create(apiUrl.toString() + path);
    }

    public HttpClient getHttpClient() {
        return HTTP_CLIENT;
    }

    public Gson getGson() {
        return GSON;
    }

    private static URI normalizeUri(String path) {
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);

        return URI.create(path);
    }
}
