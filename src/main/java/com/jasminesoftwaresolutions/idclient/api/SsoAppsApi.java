package com.jasminesoftwaresolutions.idclient.api;

import com.google.gson.reflect.TypeToken;
import com.jasminesoftwaresolutions.idclient.IDClient;
import com.jasminesoftwaresolutions.idclient.core.UnexpectedResponseException;
import com.jasminesoftwaresolutions.idclient.dto.account.SsoClientResponse;
import com.jasminesoftwaresolutions.idclient.retry.RetryableRequest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * API client for the SSO applications a principal can launch. Uses a user's
 * delegated access token (Bearer) for authorization.
 *
 * A separate type from {@link AccountApi} rather than one more method on it:
 * this answers from the identity service's {@code /accounts/me} surface but is
 * consumed by product servers proxying on the user's behalf, not by account
 * administration. No cookie jar is attached — the request carries no cookies,
 * so Identity takes the Bearer path.
 */
public class SsoAppsApi {
    private final IDClient client;
    private final String accessToken;

    public SsoAppsApi(IDClient client, String accessToken) {
        this.client = client;
        this.accessToken = accessToken;
    }

    /**
     * Lists the SSO applications the token's principal may launch
     * ({@code GET /api/v1/accounts/me/sso-clients}).
     *
     * The identity service filters the list by the caller's
     * {@code GetSSOApplication} grants and omits clients that cannot be
     * launched, so an empty list is a valid answer rather than an error.
     *
     * @return a future completing with the applications visible to the
     *         principal, possibly empty
     */
    public CompletableFuture<List<SsoClientResponse>> listSsoClients() {
        URI uri = client.getProvider().getEndpointPath("/api/v1/accounts/me/sso-clients");
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri)
                .header("Authorization", "Bearer " + accessToken)
                .build();

        return RetryableRequest.execute(
                () -> client.getProvider().getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()),
                client.getRetryPolicy())
                .thenApply(response -> parseBody(response, new TypeToken<List<SsoClientResponse>>() {}));
    }

    private <T> T parseBody(HttpResponse<String> response, TypeToken<T> typeToken) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return client.getProvider().getGson().fromJson(response.body(), typeToken.getType());
        }
        throw new UnexpectedResponseException(response.body(), typeToken.getType().getTypeName(), null);
    }
}
