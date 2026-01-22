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

package de.adorsys.keycloak.config.test.util;

import static org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS;
import static org.keycloak.OAuth2Constants.PASSWORD;

import jakarta.ws.rs.WebApplicationException;

import de.adorsys.keycloak.config.properties.KeycloakConfigProperties;
import de.adorsys.keycloak.config.util.ResponseUtil;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "run", name = "operation", havingValue = "IMPORT", matchIfMissing = true)
public class KeycloakAuthentication {

    private final KeycloakConfigProperties keycloakConfigProperties;

    @Autowired
    public KeycloakAuthentication(
            KeycloakConfigProperties keycloakConfigProperties
    ) {
        this.keycloakConfigProperties = keycloakConfigProperties;
    }

    public AccessTokenResponse login(
            String realm,
            String clientId,
            String clientSecret
    ) {
        return login(
                keycloakConfigProperties.getUrl(),
                realm,
                clientId,
                clientSecret,
                CLIENT_CREDENTIALS,
                null,
                null
        );
    }

    public AccessTokenResponse login(
            String realm,
            String clientId,
            String clientSecret,
            String username,
            String password
    ) {
        return login(
                keycloakConfigProperties.getUrl(),
                realm,
                clientId,
                clientSecret,
                username,
                password
        );
    }

    public AccessTokenResponse login(
            String url,
            String realm,
            String clientId,
            String clientSecret,
            String username,
            String password
    ) {
        return login(url, realm, clientId, clientSecret, PASSWORD, username, password);
    }

    private AccessTokenResponse login(
            String url,
            String realm,
            String clientId,
            String clientSecret,
            String grantType,
            String username,
            String password
    ) {
        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(url)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(grantType)
                .username(username)
                .password(password)
                .build()) {
            return keycloak.tokenManager().getAccessToken();
        } catch (WebApplicationException e) {
            var asd = ResponseUtil.getErrorMessage(e);
            throw new RuntimeException(e);
        }
    }
}
