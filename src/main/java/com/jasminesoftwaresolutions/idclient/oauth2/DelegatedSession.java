package com.jasminesoftwaresolutions.idclient.oauth2;

import com.jasminesoftwaresolutions.idclient.IDClient;
import com.jasminesoftwaresolutions.idclient.dto.oauth2.IntrospectResponse;
import com.jasminesoftwaresolutions.idclient.dto.oauth2.TokenResponse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A delegated OAuth2 session obtained via the authorization code flow.
 *
 * <p>Each {@code DelegatedSession} owns its own token cache, completely isolated
 * from {@link IDClient}'s service-level token management. This prevents the
 * delegated session's refresh from overwriting a service token or vice versa.</p>
 *
 * <p>Usage:
 * <pre>{@code
 *   DelegatedSession session = client.exchangeAuthorizationCode(redirectUri, code).get();
 *   String accessToken = session.getAccessToken().get();
 * }</pre>
 */
public class DelegatedSession {
    private final IDClient client;
    private volatile TokenResponse cachedToken;
    private volatile Instant tokenExpiresAt;

    /**
     * Creates a new delegated session. Typically obtained via
     * {@link IDClient#exchangeAuthorizationCode} rather than constructed directly.
     */
    public DelegatedSession(IDClient client, TokenResponse initialToken) {
        this.client = client;
        this.cachedToken = initialToken;
        this.tokenExpiresAt = Instant.now().plusSeconds(initialToken.getExpiresIn());
    }

    /**
     * Returns a valid access token, refreshing automatically if the
     * cached token has expired (with a 30-second buffer).
     *
     * <p>If no refresh token is available and the access token has expired,
     * the future completes exceptionally with an {@link IllegalStateException}.</p>
     */
    public CompletableFuture<String> getAccessToken() {
        long callId = System.nanoTime();
        String atPrefix = cachedToken != null && cachedToken.getAccessToken() != null
                ? cachedToken.getAccessToken().substring(0, Math.min(12, cachedToken.getAccessToken().length()))
                : "null";
        String rtPrefix = cachedToken != null && cachedToken.getRefreshToken() != null
                ? cachedToken.getRefreshToken().substring(0, Math.min(12, cachedToken.getRefreshToken().length()))
                : "null";

        if (cachedToken != null && tokenExpiresAt != null
                && Instant.now().plusSeconds(30).isBefore(tokenExpiresAt)) {
            System.out.printf("[DelegatedSession:%d] CACHED thread=%s atPrefix=%s expiresAt=%s%n",
                    callId, Thread.currentThread().getName(), atPrefix, tokenExpiresAt);
            return CompletableFuture.completedFuture(cachedToken.getAccessToken());
        }

        System.out.printf("[DelegatedSession:%d] EXPIRED thread=%s atPrefix=%s rtPrefix=%s%n",
                callId, Thread.currentThread().getName(), atPrefix, rtPrefix);

        if (cachedToken != null && cachedToken.getRefreshToken() != null) {
            return client.refreshAccessToken(cachedToken.getRefreshToken())
                    .thenApply(token -> {
                        this.cachedToken = token;
                        this.tokenExpiresAt = Instant.now().plusSeconds(token.getExpiresIn());
                        String newRt = token.getRefreshToken() != null ? token.getRefreshToken().substring(0, Math.min(12, token.getRefreshToken().length())) : "null";
                        System.out.printf("[DelegatedSession:%d] REFRESHED thread=%s newRtPrefix=%s → notifying onRefreshed%n",
                                callId, Thread.currentThread().getName(), newRt);
                        notifyRefreshed();
                        return token.getAccessToken();
                    });
        }

        System.out.printf("[DelegatedSession:%d] FAIL thread=%s no refresh token available%n",
                callId, Thread.currentThread().getName());
        CompletableFuture<String> failed = new CompletableFuture<>();
        failed.completeExceptionally(
                new IllegalStateException("Delegated session expired and no refresh token is available"));
        return failed;
    }

    /**
     * Introspects this session's access token.
     */
    public CompletableFuture<IntrospectResponse> introspect() {
        return client.introspectToken(cachedToken.getAccessToken());
    }

    /**
     * Returns the underlying {@link IDClient} that created this session.
     */
    public IDClient getClient() {
        return client;
    }

    /**
     * Returns the most recently cached token for this session.
     */
    public TokenResponse getCachedToken() {
        return cachedToken;
    }

    // ----- Serialization support -----

    private volatile Consumer<DelegatedSession> onRefreshed;

    /**
     * Registers a callback that fires every time the session's cached token
     * is refreshed, including on initial construction. This is the hook for
     * re-serializing the session back to a cookie or other store after a
     * transparent refresh.
     *
     * <p>Only one listener is supported; subsequent calls overwrite the previous one.</p>
     */
    public void setOnRefreshed(Consumer<DelegatedSession> listener) {
        this.onRefreshed = listener;
    }

    /**
     * Exports a serializable snapshot of this session's token cache. The
     * returned state can be persisted in a cookie, cache, or session store
     * and later restored via
     * {@link IDClient#restoreDelegatedSession(DelegatedSessionState)}.
     */
    public DelegatedSessionState toState() {
        TokenResponse t = cachedToken;
        if (t == null) {
            return new DelegatedSessionState();
        }

        long remainingSeconds;
        if (tokenExpiresAt != null) {
            remainingSeconds = Math.max(0, ChronoUnit.SECONDS.between(Instant.now(), tokenExpiresAt));
        } else {
            remainingSeconds = t.getExpiresIn();
        }

        DelegatedSessionState state = new DelegatedSessionState(
                t.getAccessToken(),
                t.getRefreshToken(),
                remainingSeconds,
                t.getRefreshTokenExpiresIn());
        // Store the absolute expiration so fromState can reconstruct it
        // without clock drift across serialize/deserialize cycles.
        if (tokenExpiresAt != null) {
            state.setAccessTokenExpiresAt(tokenExpiresAt.getEpochSecond());
        }
        return state;
    }

    /**
     * Reconstructs a live session from a serialized state snapshot and a
     * live {@link IDClient}. Prefer calling
     * {@link IDClient#restoreDelegatedSession(DelegatedSessionState)} which
     * delegates to this method.
     *
     * <p>The returned session is ready to use —
     * {@link #getAccessToken()} will refresh transparently if needed.</p>
     */
    public static DelegatedSession fromState(IDClient client, DelegatedSessionState state) {
        TokenResponse token = new TokenResponse();
        token.setAccessToken(state.getAccessToken());
        token.setRefreshToken(state.getRefreshToken());
        // Compute the correct remaining seconds from the absolute expiration
        // epoch stored by toState(). Using state.expiresIn directly causes
        // clock drift because toState saves *remaining* seconds at save time,
        // but the constructor adds them to *now* at restore time.
        long expiresIn = Math.max(0, state.getAccessTokenExpiresAt() - Instant.now().getEpochSecond());
        token.setExpiresIn(expiresIn);
        token.setRefreshTokenExpiresIn(state.getRefreshTokenExpiresIn());
        return new DelegatedSession(client, token);
    }

    private void notifyRefreshed() {
        Consumer<DelegatedSession> cb = onRefreshed;
        if (cb != null) {
            cb.accept(this);
        }
    }
}
