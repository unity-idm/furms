/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.authz.roles;

import static io.imunity.furms.domain.authz.roles.Capability.*;
import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum Role {
	CENTRAL_IDP(
		"-irrelevant-",
		"-irrelevant-",
		emptyList(),
		List.of(USERS_MAINTENANCE, OWNED_SSH_KEY_MANAGMENT)
	),
	FENIX_ADMIN(
		"furmsFenixRole",
		"ADMIN",
		List.of(
			AUTHENTICATED, PROFILE, SITE_READ, SITE_WRITE, COMMUNITY_READ, COMMUNITY_WRITE,
			FENIX_ADMINS_MANAGEMENT, READ_ALL_USERS, USERS_MAINTENANCE, OWNED_SSH_KEY_MANAGMENT
		),
		List.of(SITE_READ, SITE_WRITE, COMMUNITY_READ, COMMUNITY_WRITE, PROJECT_LIMITED_READ, REST_API_KEY_MANAGEMENT)
	),
	SITE_SUPPORT(
		"furmsSiteRole",
		"SUPPORT",
		emptyList(),
		emptyList()
	),
	SITE_ADMIN(
		"furmsSiteRole",
		"ADMIN",
		List.of(
			AUTHENTICATED, PROFILE, SITE_READ, SITE_WRITE, OWNED_SSH_KEY_MANAGMENT
		),
		List.of(READ_ALL_USERS, PROJECT_LIMITED_READ, REST_API_KEY_MANAGEMENT)
	),
	COMMUNITY_ADMIN(
		"furmsCommunityRole",
		"ADMIN",
		List.of(
			AUTHENTICATED, PROFILE, COMMUNITY_READ, COMMUNITY_WRITE, PROJECT_READ, PROJECT_WRITE, PROJECT_LIMITED_WRITE,
			PROJECT_LEAVE, PROJECT_ADMINS_MANAGEMENT, OWNED_SSH_KEY_MANAGMENT
		),
		List.of(READ_ALL_USERS, PROJECT_LIMITED_READ, REST_API_KEY_MANAGEMENT)
	),
	PROJECT_ADMIN(
		"furmsProjectRole",
		"ADMIN",
		List.of(
			AUTHENTICATED, PROFILE, PROJECT_READ, PROJECT_LIMITED_WRITE, PROJECT_ADMINS_MANAGEMENT,
			PROJECT_MEMBER_MANAGEMENT, PROJECT_LEAVE, OWNED_SSH_KEY_MANAGMENT
		),
		List.of(READ_ALL_USERS, PROJECT_LIMITED_READ, REST_API_KEY_MANAGEMENT)
	),
	PROJECT_USER(
		"furmsProjectRole",
		"USER",
		List.of(
			AUTHENTICATED, PROFILE, PROJECT_READ, PROJECT_LEAVE, OWNED_SSH_KEY_MANAGMENT
		),
		List.of(PROJECT_LIMITED_READ)
	);

	public final String unityRoleAttribute;
	public final String unityRoleValue;
	public final List<Capability> capabilities;
	public final List<Capability> globalCapabilities;

	Role(String unityRoleAttribute, String unityRoleValue, List<Capability> capabilities,
	     List<Capability> globalCapabilities) {
		this.unityRoleAttribute = unityRoleAttribute;
		this.unityRoleValue = unityRoleValue;
		this.capabilities = List.copyOf(capabilities);
		this.globalCapabilities = List.copyOf(globalCapabilities);
	}

	public static Optional<Role> translateRole(String attributeType, String value) {
		return Arrays.stream(values())
			.filter(r -> r.unityRoleAttribute.equals(attributeType))
			.filter(r -> r.unityRoleValue.equals(value))
			.findAny();
	}

}
