package com.jasminesoftwaresolutions.idclient.session;

import com.jasminesoftwaresolutions.idclient.oauth2.DelegatedSessionState;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Server-side store for {@link DelegatedSessionState} snapshots.
 *
 * <p>After an OAuth2 authorization-code exchange, the service persists the
 * session state via {@link #save} and sets a cookie containing only the
 * session ID (a random UUID). On subsequent requests the session ID is used
 * to look up the state via {@link #get}, which is then restored into a live
 * {@link com.jasminesoftwaresolutions.idclient.oauth2.DelegatedSession}.</p>
 *
 * <p>Implementations are expected to be thread-safe and to return
 * {@code CompletableFuture} for non-blocking integration with async
 * frameworks.</p>
 */
public interface SessionStore {

    /**
     * Persist a session state under the given ID with a time-to-live.
     *
     * @param sessionId unique identifier (typically a random UUID)
     * @param state     serializable snapshot of the delegated session
     * @param ttl       how long the entry should live; typically set to the
     *                  refresh-token expiry so the store self-cleans
     * @return a future that completes when the save is durable
     */
    CompletableFuture<Void> save(String sessionId, DelegatedSessionState state, Duration ttl);

    /**
     * Retrieve a previously stored session state.
     *
     * @param sessionId the identifier used at save time
     * @return a future that completes with the state, or {@code null} if the
     *         key is absent or has expired
     */
    CompletableFuture<DelegatedSessionState> get(String sessionId);

    /**
     * Remove a session from the store (logout / invalidation).
     *
     * @param sessionId the identifier to delete
     * @return a future that completes when the deletion is durable
     */
    CompletableFuture<Void> remove(String sessionId);
}
