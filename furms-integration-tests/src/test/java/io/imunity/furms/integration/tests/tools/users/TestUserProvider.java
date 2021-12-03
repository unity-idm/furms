/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.tools.users;

import io.imunity.furms.api.authz.FURMSUserProvider;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.security.core.userdetails.User;

import java.util.Map;
import java.util.Set;

public class TestUserProvider extends User implements FURMSUserProvider {
	private FURMSUser furmsUser;

	public TestUserProvider(FURMSUser furmsUser, Map<ResourceId, Set<Role>> roles) {
		super(furmsUser.id.get().id, furmsUser.firstName.get(), Set.of());
		this.furmsUser = new FURMSUser(furmsUser, roles);
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
