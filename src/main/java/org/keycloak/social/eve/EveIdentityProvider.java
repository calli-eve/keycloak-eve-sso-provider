/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.social.eve;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.jboss.logging.Logger;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;

/**
 * Modified from Discord extension by:
 * @author <a href="mailto:wadahiro@gmail.com">Hiroyuki Wada</a>
 * EVE Online extension:
 * @author calli
 */
public class EveIdentityProvider extends AbstractOAuth2IdentityProvider<EveIdentityProviderConfig>
        implements SocialIdentityProvider<EveIdentityProviderConfig> {

    private static final Logger log = Logger.getLogger(EveIdentityProvider.class);

    public static final String AUTH_URL = "https://login.eveonline.com/v2/oauth/authorize";
    public static final String TOKEN_URL = "https://login.eveonline.com/v2/oauth/token";
    public static final String VERIFY_URL = "https://esi.evetech.net/verify";
    public static final String AFFILIATION_URL = "https://esi.evetech.net/latest/characters/affiliation/?datasource=tranquility";
    public static final String DEFAULT_SCOPE = "publicData";

    public EveIdentityProvider(KeycloakSession session, EveIdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(VERIFY_URL);
    }

    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    @Override
    protected String getProfileEndpointForValidation(EventBuilder event) {
        return VERIFY_URL;
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(profile, "CharacterID"), getConfig());
        
        user.setUsername(getJsonProperty(profile, "CharacterName").strip());
        user.setFirstName(getJsonProperty(profile, "CharacterName"));
        user.setUserAttribute("corporation_id", getJsonProperty(profile, "corporation_id"));
        user.setUserAttribute("alliance_id", getJsonProperty(profile, "alliance_id"));
        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());

        return user;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        JsonNode verify = null;
        try {
            verify = SimpleHttp.doGet(VERIFY_URL, session).header("Authorization", "Bearer " + accessToken).asJson();
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not verify EVE user.", e);
        }

        JsonNode affiliation = null;
        try {
            String CharacterID = getJsonProperty(verify, "CharacterID");
            affiliation = SimpleHttp.doPost(AFFILIATION_URL, session).json(new String[]{CharacterID}).asJson();
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not get character affiliations.", e);
        }

        ObjectNode profile = mapper.createObjectNode();
        profile.put("CharacterID", getJsonProperty(verify, "CharacterID"));
        profile.put("CharacterName", getJsonProperty(verify, "CharacterName"));
        profile.put("corporation_id", affiliation.get(0).get("corporation_id").asText());
        profile.put("alliance_id", affiliation.get(0).get("alliance_id").asText());
        log.debug(profile);
        return extractIdentityFromProfile(null, profile);
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }
}
