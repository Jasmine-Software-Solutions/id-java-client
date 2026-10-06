package com.jasminesoftwaresolutions.idclient.retry;

import com.jasminesoftwaresolutions.idclient.core.ApiErrorException;
import com.jasminesoftwaresolutions.idclient.core.IDClientException;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

/**
 * Executes asynchronous HTTP requests with automatic retry.
 */
public class RetryableRequest {
    private RetryableRequest() {}

    /**
     * Executes the given async request supplier with the configured retry policy.
     * Retries on network failures (IOException) and on HTTP status codes
     * deemed retryable by the policy (e.g. 429, 5xx).
     */
    public static CompletableFuture<HttpResponse<String>> execute(
            Supplier<CompletableFuture<HttpResponse<String>>> requestSupplier,
            RetryPolicy retryPolicy) {

        CompletableFuture<HttpResponse<String>> future = new CompletableFuture<>();
        attempt(requestSupplier, retryPolicy, 0, future);
        return future;
    }

    private static void attempt(
            Supplier<CompletableFuture<HttpResponse<String>>> requestSupplier,
            RetryPolicy retryPolicy,
            int attempt,
            CompletableFuture<HttpResponse<String>> resultFuture) {

        requestSupplier.get().whenComplete((response, throwable) -> {
            if (throwable != null) {
                handleFailure(throwable, requestSupplier, retryPolicy, attempt, resultFuture);
                return;
            }

            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                resultFuture.complete(response);
                return;
            }

            // Check if this status code is retryable
            if (retryPolicy.isRetryable(status)) {
                Duration delay = retryPolicy.delayForAttempt(attempt);
                if (delay != null) {
                    scheduleRetry(requestSupplier, retryPolicy, attempt + 1, resultFuture, delay);
                    return;
                }
            }

            // Non-retryable or out of retries — fail
            resultFuture.completeExceptionally(buildException(response));
        });
    }

    private static void handleFailure(
            Throwable throwable,
            Supplier<CompletableFuture<HttpResponse<String>>> requestSupplier,
            RetryPolicy retryPolicy,
            int attempt,
            CompletableFuture<HttpResponse<String>> resultFuture) {

        // Unwrap CompletionException
        Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;

        boolean isNetworkError = cause instanceof IOException;

        if (isNetworkError) {
            Duration delay = retryPolicy.delayForAttempt(attempt);
            if (delay != null) {
                scheduleRetry(requestSupplier, retryPolicy, attempt + 1, resultFuture, delay);
                return;
            }
        }

        resultFuture.completeExceptionally(cause instanceof IDClientException
                ? cause
                : new IDClientException("Request failed: " + cause.getMessage(), cause));
    }

    private static void scheduleRetry(
            Supplier<CompletableFuture<HttpResponse<String>>> requestSupplier,
            RetryPolicy retryPolicy,
            int nextAttempt,
            CompletableFuture<HttpResponse<String>> resultFuture,
            Duration delay) {

        CompletableFuture.delayedExecutor(delay.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .execute(() -> attempt(requestSupplier, retryPolicy, nextAttempt, resultFuture));
    }

    private static ApiErrorException buildException(HttpResponse<String> response) {
        String error = "unknown_error";
        String errorDescription = null;

        try {
            var json = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
            if (json.has("error")) error = json.get("error").getAsString();
            if (json.has("error_description")) errorDescription = json.get("error_description").getAsString();
        } catch (Exception ignored) {
            errorDescription = response.body();
        }

        return new ApiErrorException(response.statusCode(), error, errorDescription);
    }
}
