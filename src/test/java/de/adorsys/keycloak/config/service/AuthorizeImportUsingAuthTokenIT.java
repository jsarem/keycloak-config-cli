/*-
 * ---license-start
 * keycloak-config-cli
 * ---
 * Copyright (C) 2017 - 2021 adorsys GmbH & Co. KG @ https://adorsys.com
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package de.adorsys.keycloak.config.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import de.adorsys.keycloak.config.AbstractImportIT;

class AuthorizeImportUsingAuthTokenIT extends AbstractImportIT {
    private static final String REALM_NAME = "auth-token-realm";

    private static String authToken;

    public AuthorizeImportUsingAuthTokenIT() {
        this.resourcePath = "import-files/auth-token";
    }

    @Test
    @Order(0)
    void initializeServiceAccount() throws IOException {
        // Create a service account to the master realm to be used later
        doImport("00_update_realm_create_service_account_in_master_realm.json");

        AccessTokenResponse token = keycloakAuthentication.login(
                "master",
                "auth-token-master",
                "auth-token-master-secret"
        );

        assertThat(token.getToken(), notNullValue());
        authToken = token.getToken();
    }

    @Nested
    @Order(1)
    class AuthenticateUsingAuthToken {

        @Test
        void createRealm() throws IOException {
            doImport("01_create_realm_with_auth_token.json");

            Keycloak keycloak = keycloakProvider.getInstance();
            Assertions.assertNull(keycloak.tokenManager());

            RealmRepresentation realm = keycloak.realm(REALM_NAME).toRepresentation();

            assertThat(realm.getRealm(), is(REALM_NAME));
            assertThat(realm.isEnabled(), is(true));
        }

        @Test
        void refreshToken() {
            // Static auth token cannot be refreshed, but the refresh should not throw an error
            Assertions.assertDoesNotThrow(() -> keycloakProvider.refreshToken());
        }

        @Test
        void logout() {
            // Static auth token cannot be revoked either, but should not throw an error
            Assertions.assertDoesNotThrow(() -> keycloakProvider.close());
        }

        @DynamicPropertySource
        static void registerProperties(DynamicPropertyRegistry registry) {
            registry.add("keycloak.auth-token", () -> authToken);
            registry.add("keycloak.client-id", () -> "bogus");
            registry.add("keycloak.client-secret", () -> "bogus");
            registry.add("keycloak.username", () -> "bogus");
            registry.add("keycloak.password", () -> "bogus");
        }
    }

}
