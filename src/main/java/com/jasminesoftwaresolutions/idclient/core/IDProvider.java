package com.jasminesoftwaresolutions.idclient.core;

import com.google.gson.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Instant;

/**
 * Provider configuration that holds the base URLs and shared HTTP/Gson instances.
 */
public class IDProvider {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.toEpochMilli()))
            .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, typeOfT, context) ->
                    Instant.ofEpochMilli(json.getAsLong()))
            .create();

    private final URI url;

    public IDProvider(URI url) {
        this.url = url;
    }

    public IDProvider(String url) {
        this.url = normalizeUri(url);
    }

    public URI getEndpointPath(String path) {
        String url = this.url.toString();
        if (url.endsWith("/"))
            url = url.substring(0, url.length() - 1);

        if (path.startsWith("/"))
            path = path.substring(1);

        return URI.create(url + "/" + path);
    }

    public URI getUrl() {
        return url;
    }

    public HttpClient getHttpClient() {
        return HTTP_CLIENT;
    }

    public Gson getGson() {
        return GSON;
    }

    private static URI normalizeUri(String uri) {
        if (uri.endsWith("/"))
            uri = uri.substring(0, uri.length() - 1);
        return URI.create(uri);
    }
}
