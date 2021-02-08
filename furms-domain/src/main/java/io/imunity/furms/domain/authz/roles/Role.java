/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.authz.roles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.imunity.furms.domain.authz.roles.Capability.*;
import static java.util.Collections.emptyList;

public enum Role {
	FENIX_ADMIN(
		"furmsFenixRole",
		"ADMIN",
		List.of(
			AUTHENTICATED, PROFILE, SITE_READ, SITE_WRITE, COMMUNITY_READ, COMMUNITY_WRITE,
			FENIX_ADMINS_MANAGEMENT, READ_ALL_USERS
		),
		List.of(
			SITE_READ, SITE_WRITE, COMMUNITY_READ, COMMUNITY_WRITE
		)
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
			AUTHENTICATED, PROFILE, SITE_READ, SITE_WRITE, SITE_ADMINS_MANAGEMENT
		),
		List.of(READ_ALL_USERS)
	),
	COMMUNITY_ADMIN(
		"furmsCommunityRole",
		"ADMIN",
		List.of(
			AUTHENTICATED, PROFILE, COMMUNITY_READ, COMMUNITY_WRITE, PROJECT_READ, PROJECT_WRITE, PROJECT_LIMITED_WRITE,
			COMMUNITY_ADMINS_MANAGEMENT
		),
		List.of(READ_ALL_USERS)
	),
	PROJECT_ADMIN(
		"furmsProjectRole",
		"ADMIN",
		List.of(
			AUTHENTICATED, PROFILE, PROJECT_READ, PROJECT_LIMITED_WRITE, PROJECT_ADMINS_MANAGEMENT,
			PROJECT_MEMBER_MANAGEMENT, PROJECT_LEAVE
		),
		List.of(READ_ALL_USERS)
	),
	PROJECT_USER(
		"furmsProjectRole",
		"USER",
		List.of(
			AUTHENTICATED, PROFILE, PROJECT_READ, PROJECT_LEAVE
		),
		List.of(PROJECT_READ)
	);

	public final String unityRoleAttribute;
	public final String unityRoleValue;
	public final List<Capability> capabilities;
	public final List<Capability> additionalCapabilities;

	Role(String unityRoleAttribute, String unityRoleValue, List<Capability> capabilities,
	     List<Capability> additionalCapabilities) {
		this.unityRoleAttribute = unityRoleAttribute;
		this.unityRoleValue = unityRoleValue;
		this.capabilities = List.copyOf(capabilities);
		this.additionalCapabilities = List.copyOf(additionalCapabilities);
	}

	public static Optional<Role> translateRole(String attributeType, String value) {
		return Arrays.stream(values())
			.filter(r -> r.unityRoleAttribute.equals(attributeType))
			.filter(r -> r.unityRoleValue.equals(value))
			.findAny();
	}

}
