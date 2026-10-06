package com.jasminesoftwaresolutions.idclient.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.Instant;

/**
 * Utility helpers for parsing common fields from JSON responses.
 */
public final class JsonHelper {
    private JsonHelper() {}

    public static String getStringOrNull(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) ? el.getAsString() : null;
    }

    public static Instant getInstantOrNull(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) ? Instant.ofEpochMilli(el.getAsLong()) : null;
    }

    public static boolean getBooleanOrFalse(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) && el.getAsBoolean();
    }
}
