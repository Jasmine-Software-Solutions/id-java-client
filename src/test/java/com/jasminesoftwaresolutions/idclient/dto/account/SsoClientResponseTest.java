package com.jasminesoftwaresolutions.idclient.dto.account;

import com.google.gson.reflect.TypeToken;
import com.jasminesoftwaresolutions.idclient.core.IDProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pins the wire spelling of {@link SsoClientResponse}.
 *
 * The DTO is parsed by {@code IDProvider}'s Gson, which has no field-naming
 * policy — a missing {@code @SerializedName} leaves the field null without any
 * error. The identity service writes {@code LOWER_CASE_WITH_UNDERSCORES}, so
 * this test parses exactly that and asserts every field survived.
 */
class SsoClientResponseTest {

    private static final String CLIENT_ID = "5f0c1e36-8a4e-4a1e-9d2b-2f6a9c1d3e7b";

    @Test
    void parsesEveryFieldOfTheSnakeCaseWireShape() {
        String body = """
                [{
                  "client_id": "5f0c1e36-8a4e-4a1e-9d2b-2f6a9c1d3e7b",
                  "fqdn": "forms.localhost",
                  "name": "Jasmine Forms",
                  "logo_uri": "https://example.test/forms.png",
                  "initiate_login_uri": "https://forms.localhost/api/v1/auth/login"
                }]
                """;

        List<SsoClientResponse> apps = new IDProvider("http://localhost").getGson()
                .fromJson(body, new TypeToken<List<SsoClientResponse>>() {}.getType());

        assertEquals(1, apps.size());
        SsoClientResponse app = apps.get(0);
        assertEquals(UUID.fromString(CLIENT_ID), app.getClientId());
        assertEquals("forms.localhost", app.getFqdn());
        assertEquals("Jasmine Forms", app.getName());
        assertEquals("https://example.test/forms.png", app.getLogoUri());
        assertEquals("https://forms.localhost/api/v1/auth/login", app.getInitiateLoginUri());
    }

    @Test
    void leavesAbsentOptionalFieldsNull() {
        // Identity omits null fields from its response, so "no name" and "no
        // logo" arrive as absent keys rather than JSON nulls.
        String body = """
                [{
                  "client_id": "5f0c1e36-8a4e-4a1e-9d2b-2f6a9c1d3e7b",
                  "fqdn": "forms.localhost",
                  "initiate_login_uri": "https://forms.localhost/api/v1/auth/login"
                }]
                """;

        List<SsoClientResponse> apps = new IDProvider("http://localhost").getGson()
                .fromJson(body, new TypeToken<List<SsoClientResponse>>() {}.getType());

        assertNull(apps.get(0).getName());
        assertNull(apps.get(0).getLogoUri());
    }
}
