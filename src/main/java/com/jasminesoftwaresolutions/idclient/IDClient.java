package com.jasminesoftwaresolutions.idclient;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jasminesoftwaresolutions.idclient.core.IDProvider;
import com.jasminesoftwaresolutions.idclient.core.UnexpectedResponseException;
import com.jasminesoftwaresolutions.idclient.dto.oauth2.TokenResponse;
import com.jasminesoftwaresolutions.idclient.dto.oauth2.IntrospectResponse;
import com.jasminesoftwaresolutions.idclient.oauth2.ClientCredentialsFlow;
import com.jasminesoftwaresolutions.idclient.oauth2.DelegatedSession;
import com.jasminesoftwaresolutions.idclient.oauth2.DelegatedSessionState;
import com.jasminesoftwaresolutions.idclient.oauth2.RefreshTokenFlow;
import com.jasminesoftwaresolutions.idclient.retry.RetryPolicy;
import com.jasminesoftwaresolutions.idclient.retry.RetryableRequest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An OAuth2 client registered with the ID server. Authenticates via HTTP Basic Auth
 * (client ID + secret) and supports all standard OAuth2 grant flows.
 */
public class IDClient {
    private final IDProvider provider;
    private final RetryPolicy retryPolicy;

    private final UUID id;
    private final String secret;

    /**
     * Ensures only one inflight refresh per token value across concurrent
     * requests.  Without this, two requests carrying the same refresh token
     * in their cookies would independently call the ID server — the first
     * succeeds and rotates the token, the second fails with 403 because its
     * token was already consumed, triggering a spurious 302 redirect.
     */
    private final ConcurrentHashMap<String, CompletableFuture<TokenResponse>> pendingRefreshes =
            new ConcurrentHashMap<>();

    public IDClient(IDProvider provider, UUID id, String secret) {
        this(provider, id, secret, RetryPolicy.defaultPolicy());
    }

    public IDClient(IDProvider provider, UUID id, String secret, RetryPolicy retryPolicy) {
        this.provider = provider;
        this.id = id;
        this.secret = secret;
        this.retryPolicy = retryPolicy;
    }

    // ----- Authentication -----

    /**
     * Returns the HTTP Basic Auth header value for this client.
     */
    public String getClientCredentials() {
        String decoded = id + ":" + secret;
        return "Basic " + Base64.getEncoder().encodeToString(decoded.getBytes());
    }

    // ----- Client Credentials flow -----

    /**
     * Requests an API token using the client credentials grant.
     * This is a one-shot call — no caching is performed.
     */
    public CompletableFuture<TokenResponse> requestApiToken() {
        return requestApiToken(null, null);
    }

    /**
     * Requests an API token scoped to a specific tenant.
     * This is a one-shot call — no caching is performed.
     */
    public CompletableFuture<TokenResponse> requestApiToken(UUID tenant) {
        return requestApiToken(tenant, null);
    }

    /**
     * Requests an API token with the given scope and optional tenant.
     * This is a one-shot call — no caching is performed.
     */
    public CompletableFuture<TokenResponse> requestApiToken(UUID tenant, String scope) {
        URI tokenUri = ClientCredentialsFlow.buildTokenUri(provider, tenant, scope);
        return executeTokenRequest(tokenUri)
                .thenApply(this::parseTokenResponse);
    }

    // ----- Authorization Code flow -----

    /**
     * Exchanges an authorization code for a delegated session.
     *
     * <p>The returned {@link DelegatedSession} manages its own token lifecycle —
     * it is completely isolated from this client's service-level token management.</p>
     */
    public CompletableFuture<DelegatedSession> exchangeAuthorizationCode(URI redirectUri, String code) {
        URI tokenUri = com.jasminesoftwaresolutions.idclient.oauth2.AuthorizationCodeFlow
                .buildTokenUri(provider, id, redirectUri, code);
        return executeTokenRequest(tokenUri)
                .thenApply(this::parseTokenResponse)
                .thenApply(token -> new DelegatedSession(this, token));
    }

    // ----- Refresh Token flow -----

    /**
     * Refreshes an access token using a refresh token.
     * This is a one-shot call — no caching is performed.
     */
    public CompletableFuture<TokenResponse> refreshAccessToken(String refreshToken) {
        // Log with a unique per-invocation tag so we can correlate log lines
        long callId = System.nanoTime();
        String rtPreview = refreshToken.length() > 12 ? refreshToken.substring(0, 12) : refreshToken;
        String mapKey = refreshToken;

        System.out.printf("[IDClient-refresh:%d] ENTER  thread=%s rtPrefix=%s mapSize=%d%n",
                callId, Thread.currentThread().getName(), rtPreview, pendingRefreshes.size());

        CompletableFuture<TokenResponse> result = pendingRefreshes.computeIfAbsent(mapKey, rt -> {
            System.out.printf("[IDClient-refresh:%d] MISS   thread=%s rtPrefix=%s → creating new future, starting HTTP call%n",
                    callId, Thread.currentThread().getName(), rtPreview);
            URI tokenUri = RefreshTokenFlow.buildTokenUri(provider, rt);
            return executeTokenRequest(tokenUri)
                    .thenApply(resp -> {
                        TokenResponse tr = parseTokenResponse(resp);
                        String newRtPreview = tr.getRefreshToken() != null && tr.getRefreshToken().length() > 12
                                ? tr.getRefreshToken().substring(0, 12) : "null";
                        System.out.printf("[IDClient-refresh:%d] OK     thread=%s oldRtPrefix=%s newRtPrefix=%s status=%d%n",
                                callId, Thread.currentThread().getName(), rtPreview, newRtPreview, resp.statusCode());
                        return tr;
                    })
                    .whenComplete((resp, err) -> {
                        Object removed = pendingRefreshes.remove(mapKey);
                        System.out.printf("[IDClient-refresh:%d] CLEAN  thread=%s rtPrefix=%s removed=%s err=%s mapSize=%d%n",
                                callId, Thread.currentThread().getName(), rtPreview,
                                removed != null, err != null ? err.getClass().getSimpleName() : "null",
                                pendingRefreshes.size());
                    });
        });

        if (result.isDone()) {
            System.out.printf("[IDClient-refresh:%d] HIT    thread=%s rtPrefix=%s → joined already-completed future%n",
                    callId, Thread.currentThread().getName(), rtPreview);
        } else {
            System.out.printf("[IDClient-refresh:%d] HIT    thread=%s rtPrefix=%s → joined inflight future%n",
                    callId, Thread.currentThread().getName(), rtPreview);
        }

        return result;
    }

    // ----- Token management -----

    /**
     * Convenience method: requests a service token via client credentials
     * and returns the access token string. No caching is performed — each
     * call issues a new token request.
     */
    public CompletableFuture<String> serviceToken() {
        return requestApiToken().thenApply(TokenResponse::getAccessToken);
    }

    /**
     * Reconstructs a live {@link DelegatedSession} from a serialized state
     * snapshot. This is the inverse of {@link DelegatedSession#toState()}.
     *
     * <p>Typical cookie-based usage:
     * <pre>{@code
     *   // storing
     *   var state = session.toState();
     *   var json = gson.toJson(state);
     *   response.addCookie(new Cookie("session", encrypt(json), ...));
     *
     *   // restoring
     *   var state = gson.fromJson(decrypt(cookie), DelegatedSessionState.class);
     *   var session = client.restoreDelegatedSession(state);
     *   session.setOnRefreshed(s -> reSerializeCookie(s));
     *   String token = session.getAccessToken().get();  // auto-refreshes if needed
     * }</pre>
     */
    public DelegatedSession restoreDelegatedSession(DelegatedSessionState state) {
        return DelegatedSession.fromState(this, state);
    }

    /**
     * Introspects a token using this client's own credentials for authorization.
     */
    public CompletableFuture<IntrospectResponse> introspectToken(String token) {
        URI endpoint = provider.getEndpointPath("/api/v1/oauth2/introspect?token=" + token);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(endpoint)
                .header("Authorization", getClientCredentials())
                .build();

        return RetryableRequest.execute(
                () -> provider.getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()),
                retryPolicy)
                .thenApply(HttpResponse::body)
                .thenApply(body -> provider.getGson()
                        .fromJson(body, IntrospectResponse.class));
    }

    // ----- Internal helpers -----

    private CompletableFuture<HttpResponse<String>> executeTokenRequest(URI tokenUri) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(tokenUri)
                .header("Authorization", getClientCredentials())
                .build();

        System.out.printf("[IDClient-http] REQUEST thread=%s uri=%s%n",
                Thread.currentThread().getName(), tokenUri);

        return RetryableRequest.execute(
                () -> provider.getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()),
                retryPolicy)
                .thenApply(resp -> {
                    System.out.printf("[IDClient-http] RESPONSE thread=%s status=%d bodyLen=%d%n",
                            Thread.currentThread().getName(), resp.statusCode(),
                            resp.body() != null ? resp.body().length() : 0);
                    if (resp.statusCode() >= 400) {
                        System.out.printf("[IDClient-http] ERROR thread=%s status=%d body=%s%n",
                                Thread.currentThread().getName(), resp.statusCode(), resp.body());
                    }
                    return resp;
                });
    }

    private TokenResponse parseTokenResponse(HttpResponse<String> response) {
        String body = response.body();
        try {
            var json = JsonParser.parseString(body).getAsJsonObject();
            long expiresIn = json.get("expires_in").getAsLong();
            Long refreshExpiresIn = json.has("refresh_token_expires_in")
                    ? json.get("refresh_token_expires_in").getAsLong() : null;

            TokenResponse token = new TokenResponse();
            token.setTokenType(json.get("token_type").getAsString());
            token.setAccessToken(json.get("access_token").getAsString());
            token.setExpiresIn(expiresIn);
            if (json.has("refresh_token"))
                token.setRefreshToken(json.get("refresh_token").getAsString());
            token.setRefreshTokenExpiresIn(refreshExpiresIn);
            if (json.has("scope"))
                token.setScope(json.get("scope").getAsString());
            return token;
        } catch (JsonSyntaxException | IllegalStateException ex) {
            throw new UnexpectedResponseException(body, "Token JSON", ex);
        }
    }

    // ----- Accessors -----

    public IDProvider getProvider() {
        return provider;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public UUID getId() {
        return id;
    }

}
