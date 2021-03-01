/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.core.config.security.rest;

import io.imunity.furms.api.authz.FURMSUserProvider;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

class PresetUser extends User implements FURMSUserProvider {
	private FURMSUser furmsUser;

	PresetUser(String username, String password,
			Collection<? extends GrantedAuthority> authorities, 
			Map<ResourceId, Set<Role>> roles) {
		this(
			username,
			password,
			authorities,
			new FURMSUser(username, "Central Idp", "User", null, roles)
		);
	}

	PresetUser(String username, String password,
	           Collection<? extends GrantedAuthority> authorities,
	           FURMSUser furmsUser) {
		super(username, password, authorities);
		this.furmsUser = furmsUser;
	}

	PresetUser(PresetUser user) {
		this(user.getUsername(), user.getPassword(), user.getAuthorities(), new FURMSUser(user.furmsUser));
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