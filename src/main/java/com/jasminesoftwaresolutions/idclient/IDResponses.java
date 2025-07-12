package com.jasminesoftwaresolutions.idclient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.Instant;

public class IDResponses {
    public static String getAsStringOrNull(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) ? el.getAsString() : null;
    }

    public static Instant getAsInstantOrNull(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) ? Instant.ofEpochMilli(el.getAsLong()) : null;
    }

    public static boolean getAsBooleanOrFalse(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) && el.getAsBoolean();
    }
}
