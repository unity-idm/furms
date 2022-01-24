/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.performance.tests;

import io.imunity.furms.api.authz.FURMSUserProvider;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;

public class SecurityUserUtils {

	public static void createSecurityUser(Map<ResourceId, Set<Role>> extraRoles) {
		SecurityContextHolder.getContext().setAuthentication(null);
		final Map<ResourceId, Set<Role>> roles = new HashMap<>();
		roles.put(new ResourceId((UUID) null, APP_LEVEL), Set.of(Role.values()));
		roles.putAll(extraRoles);

		final FURMSUser userWithRoles = new FURMSUser(FURMSUser.builder()
				.id(new PersistentId(UUID.randomUUID().toString()))
				.email("testuser@domain.com")
				.build(),
				roles);
		final Authentication authResult = new UsernamePasswordAuthenticationToken(
				new TestUser(userWithRoles.id.get().id, UUID.randomUUID().toString(), userWithRoles),
				null, Set.of());
		SecurityContextHolder.getContext().setAuthentication(authResult);
	}

	private static class TestUser extends User implements FURMSUserProvider {

		private FURMSUser furmsUser;

		TestUser(String username, String password, FURMSUser furmsUser) {
			super(username, password, Set.of());
			this.furmsUser = furmsUser;
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
}
