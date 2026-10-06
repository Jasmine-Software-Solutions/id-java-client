package com.jasminesoftwaresolutions.idclient.session;

import com.google.gson.Gson;
import com.jasminesoftwaresolutions.idclient.oauth2.DelegatedSessionState;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Redis-backed implementation of {@link SessionStore}.
 *
 * <p>Session state is serialized as plain JSON (no encryption — rely on
 * Redis network isolation and the unguessable session ID). Keys follow the
 * pattern {@code {keyPrefix}{sessionId}} (e.g. {@code "session:"} →
 * {@code session:abc123}).</p>
 *
 * <p>Uses the Lettuce async API throughout. A single connection is opened
 * at construction time and held for the lifetime of the store. Call
 * {@link #close()} when the store is no longer needed.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 *   RedisSessionStore store = new RedisSessionStore(
 *       RedisURI.create("redis://localhost:6379"), "session:");
 *   store.save("abc123", state, Duration.ofHours(24));
 *   DelegatedSessionState restored = store.get("abc123").get();
 *   store.close();
 * }</pre>
 */
public class RedisSessionStore implements SessionStore, AutoCloseable {

    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisAsyncCommands<String, String> commands;
    private final String keyPrefix;
    private final Gson gson;

    /**
     * Creates a new Redis-backed session store.
     *
     * @param redisUri  Redis connection URI (e.g. {@code redis://localhost:6379})
     * @param keyPrefix prefix prepended to every key (e.g. {@code "session:"})
     */
    public RedisSessionStore(RedisURI redisUri, String keyPrefix) {
        this.keyPrefix = keyPrefix;
        this.gson = new Gson();
        this.redisClient = RedisClient.create(redisUri);
        this.connection = redisClient.connect();
        this.commands = connection.async();
    }

    /**
     * Creates a new Redis-backed session store with a custom Gson instance.
     *
     * @param redisUri  Redis connection URI
     * @param keyPrefix prefix prepended to every key
     * @param gson      Gson instance to use for serialization
     */
    public RedisSessionStore(RedisURI redisUri, String keyPrefix, Gson gson) {
        this.keyPrefix = keyPrefix;
        this.gson = gson;
        this.redisClient = RedisClient.create(redisUri);
        this.connection = redisClient.connect();
        this.commands = connection.async();
    }

    private String redisKey(String sessionId) {
        return keyPrefix + sessionId;
    }

    @Override
    public CompletableFuture<Void> save(String sessionId, DelegatedSessionState state, Duration ttl) {
        String key = redisKey(sessionId);
        String value = gson.toJson(state);
        return commands.setex(key, ttl.getSeconds(), value)
                .toCompletableFuture()
                .thenApply(ignored -> null);
    }

    @Override
    public CompletableFuture<DelegatedSessionState> get(String sessionId) {
        String key = redisKey(sessionId);
        return commands.get(key)
                .toCompletableFuture()
                .thenApply(json -> {
                    if (json == null) return null;
                    return gson.fromJson(json, DelegatedSessionState.class);
                });
    }

    @Override
    public CompletableFuture<Void> remove(String sessionId) {
        String key = redisKey(sessionId);
        return commands.del(key)
                .toCompletableFuture()
                .thenApply(ignored -> null);
    }

    @Override
    public void close() {
        connection.close();
        redisClient.shutdown();
    }
}
