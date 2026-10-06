package com.jasminesoftwaresolutions.idclient.dto.oauth2;

import com.google.gson.annotations.SerializedName;

/**
 * Introspection response from {@code POST /api/v1/oauth2/introspect}.
 */
public class IntrospectResponse {
    private boolean active;

    @SerializedName("token_type")
    private String tokenType;

    private String scope;

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("tenant_id")
    private String tenantId;

    private String sub;

    private long exp;
    private long iat;

    public IntrospectResponse() {}

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }

    public long getExp() { return exp; }
    public void setExp(long exp) { this.exp = exp; }

    public long getIat() { return iat; }
    public void setIat(long iat) { this.iat = iat; }
}
