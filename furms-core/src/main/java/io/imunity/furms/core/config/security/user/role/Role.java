/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user.role;

import io.imunity.furms.core.config.security.user.capability.Capability;

import java.util.Arrays;
import java.util.List;

import static io.imunity.furms.core.config.security.user.capability.Capability.*;
import static java.util.Collections.emptyList;

public enum Role {
	FENIX_ADMIN(
		"furmsFenixRole",
		"ADMIN",
			List.of(
				AUTHENTICATED, PROFILE, SITE_READ, SITE_WRITE, COMMUNITY_READ, COMMUNITY_WRITE,
				FENIX_ADMINS_MANAGEMENT
			),
			List.of(SITE_READ, SITE_WRITE, COMMUNITY_READ, COMMUNITY_WRITE)
		),
		FENIX_SUPPORT(
			"furmsFenixRole",
			"SUPPORT",
			emptyList(),
			emptyList()
		),
		SITE_ADMIN(
			"furmsSiteRole",
			"SUPPORT",
			List.of(
				AUTHENTICATED, PROFILE, SITE_READ, SITE_WRITE, SITE_ADMINS_MANAGEMENT
			),
			emptyList()
		),
		COMMUNITY_ADMIN(
			"furmsCommunityRole",
			"SUPPORT",
			List.of(
			AUTHENTICATED, PROFILE, COMMUNITY_READ, COMMUNITY_WRITE, PROJECT_READ, PROJECT_WRITE,
			COMMUNITY_ADMINS_MANAGEMENT
			),
			emptyList()
		),
		PROJECT_ADMIN(
			"furmsProjectRole",
			"SUPPORT",
			List.of(
			AUTHENTICATED, PROFILE, PROJECT_READ, PROJECT_WRITE, PROJECT_ADMINS_MANAGEMENT,
			PROJECT_MEMBER_MANAGEMENT
			),
			emptyList()
		),
		PROJECT_MEMBER(
		"furmsProjectRole",
		"SUPPORT",
		List.of(
			AUTHENTICATED, PROFILE, PROJECT_READ
		),
		emptyList()
	);


	public final String unityRoleAttribute;
	public final String unityRoleValue;
	public final List<Capability> capabilities;
	public final List<Capability> additionalCapabilities;

	Role(String unityRoleAttribute, String unityRoleValue, List<Capability> capabilities, List<Capability> additionalCapabilities) {
		this.unityRoleAttribute = unityRoleAttribute;
		this.unityRoleValue = unityRoleValue;
		this.capabilities = List.copyOf(capabilities);
		this.additionalCapabilities = List.copyOf(additionalCapabilities);
	}

	public static Role translateRole(String attributeType, String value){
		return Arrays.stream(values())
			.filter(r -> r.unityRoleAttribute.equals(attributeType))
			.filter(r -> r.unityRoleAttribute.equals(value))
			.findAny()
			.orElseThrow(() -> new RuntimeException("Invalid attributeType: " + attributeType + " or value: " + value));
	}

}
