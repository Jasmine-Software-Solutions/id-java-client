package com.jasminesoftwaresolutions.idclient.oauth2;

/**
 * A lightweight, serializable snapshot of a {@link DelegatedSession}'s token
 * cache. Contains no references to {@link com.jasminesoftwaresolutions.idclient.IDClient}
 * or other non-serializable infrastructure, making it safe to store in cookies,
 * distributed caches, or HTTP session attributes.
 *
 * <p>Reconstruct a live session via
 * {@link com.jasminesoftwaresolutions.idclient.IDClient#restoreDelegatedSession(DelegatedSessionState)}.</p>
 *
 * <p>All fields are simple types — this class is safe to serialize with Gson out of the box.</p>
 */
public class DelegatedSessionState {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private Long refreshTokenExpiresIn;
    /**
     * Absolute epoch second when the access token expires.
     * When present, {@link DelegatedSession#fromState} uses this directly
     * instead of computing {@code now + expiresIn}, avoiding clock drift
     * across serialize/deserialize cycles.
     */
    private Long accessTokenExpiresAt;

    /** For Gson deserialization. */
    public DelegatedSessionState() {}

    /**
     * @param accessToken           the bearer access token
     * @param refreshToken          the refresh token (may be null)
     * @param expiresIn             seconds until the access token expires
     * @param refreshTokenExpiresIn seconds until the refresh token expires (may be null)
     */
    public DelegatedSessionState(String accessToken, String refreshToken,
                                  long expiresIn, Long refreshTokenExpiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    /** @return remaining seconds until the access token expires (may be stale after serialization) */
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public Long getRefreshTokenExpiresIn() { return refreshTokenExpiresIn; }
    public void setRefreshTokenExpiresIn(Long refreshTokenExpiresIn) { this.refreshTokenExpiresIn = refreshTokenExpiresIn; }

    /** @return absolute epoch second when the access token expires, or null if not set */
    public Long getAccessTokenExpiresAt() { return accessTokenExpiresAt; }
    public void setAccessTokenExpiresAt(Long accessTokenExpiresAt) { this.accessTokenExpiresAt = accessTokenExpiresAt; }
}
