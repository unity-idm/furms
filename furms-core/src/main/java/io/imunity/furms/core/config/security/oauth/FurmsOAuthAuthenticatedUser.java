/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.oauth;

import io.imunity.furms.api.authz.FURMSUserProvider;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.constant.CommonAttribute;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Set;

class FurmsOAuthAuthenticatedUser extends DefaultOAuth2User implements FURMSUserProvider {
	public FURMSUser furmsUser;

	FurmsOAuthAuthenticatedUser(OAuth2User defaultOAuth2User, String key, Map<ResourceId, Set<Role>> roles) {
		super(defaultOAuth2User.getAuthorities(), defaultOAuth2User.getAttributes(), key);
		this.furmsUser = FURMSUser.builder()
				.id(new PersistentId(getAttribute("sub")))
				.firstName(getAttribute(CommonAttribute.FIRSTNAME.name))
				.lastName(getAttribute(CommonAttribute.SURNAME.name))
				.email(getAttribute(CommonAttribute.EMAIL.name))
				.roles(roles)
				.build();
	}

	@Override
	public FURMSUser getFURMSUser() {
		return furmsUser;
	}

	@Override
	public void updateFURMSUser(FURMSUser furmsUser) {
		this.furmsUser = furmsUser;
	}
}
