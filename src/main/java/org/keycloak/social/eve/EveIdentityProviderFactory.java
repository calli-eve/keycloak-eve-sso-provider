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

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
/**
 * Discord version:
 * @author <a href="mailto:wadahiro@gmail.com">Hiroyuki Wada</a>
 * Eve version:
 * @author calli
 */
public class EveIdentityProviderFactory extends AbstractIdentityProviderFactory<EveIdentityProvider>
        implements SocialIdentityProviderFactory<EveIdentityProvider> {

    public static final String PROVIDER_ID = "eve-online";

    @Override
    public String getName() {
        return "EVE Online";
    }

    @Override
    public EveIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new EveIdentityProvider(session, new EveIdentityProviderConfig(model));
    }

    @Override
    public EveIdentityProviderConfig createConfig() {
        return new EveIdentityProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
