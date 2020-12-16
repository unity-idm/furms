/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user.role;

import io.imunity.furms.core.config.security.user.capability.Capability;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.imunity.furms.core.config.security.user.capability.Capability.*;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public enum  Role {
	;
	private static final Map<String, FurmsRole[]> roleTranslator = Map.of(
		FENIX_ROLE.roleName, FENIX_ROLE.values(),
		SITE_ROLE.roleName, SITE_ROLE.values(),
		COMMUNITY_ROLE.roleName, COMMUNITY_ROLE.values(),
		PROJECT_ROLE.roleName, PROJECT_ROLE.values()
	);

	public enum FENIX_ROLE implements FurmsRole, SpecialRole {
		ADMIN(
			List.of(
				AUTHENTICATED, PROFILE, SITE_READ, SITE_WRITE, COMMUNITY_READ, COMMUNITY_WRITE,
				FENIX_ADMINS_MANAGEMENT
			),
			List.of(SITE_READ, SITE_WRITE, COMMUNITY_READ, COMMUNITY_WRITE)
		),
		SUPPORT(emptyList(), emptyList());

		private final List<Capability> capabilities;
		private final List<Capability> additionalCapabilities;
		private final static String roleName = "furmsFenixRole";

		FENIX_ROLE(List<Capability> capabilities, List<Capability> additionalCapabilities){
			this.capabilities = capabilities;
			this.additionalCapabilities = additionalCapabilities;
		}

		@Override
		public List<Capability> getCapabilities() {
			return capabilities;
		}

		@Override
		public List<Capability> getAdditionalCapabilities() {
			return additionalCapabilities;
		}
	}

	enum SITE_ROLE implements FurmsRole {
		ADMIN(List.of(
			AUTHENTICATED, PROFILE, SITE_READ, SITE_WRITE, SITE_ADMINS_MANAGEMENT
		));

		private final List<Capability> capabilities;
		private final static String roleName = "furmsSiteRole";

		SITE_ROLE(List<Capability> capabilities){
			this.capabilities = capabilities;
		}

		@Override
		public List<Capability> getCapabilities() {
			return capabilities;
		}
	}

	enum COMMUNITY_ROLE implements FurmsRole {
		ADMIN(List.of(
			AUTHENTICATED, PROFILE, COMMUNITY_READ, COMMUNITY_WRITE, PROJECT_READ, PROJECT_WRITE,
			COMMUNITY_ADMINS_MANAGEMENT
		));

		private final List<Capability> capabilities;
		private final static String roleName = "furmsCommunityRole";

		COMMUNITY_ROLE(List<Capability> capabilities){
			this.capabilities = capabilities;
		}

		@Override
		public List<Capability> getCapabilities() {
			return capabilities;
		}
	}

	enum PROJECT_ROLE implements FurmsRole {
		ADMIN(List.of(
			AUTHENTICATED, PROFILE, PROJECT_READ, PROJECT_WRITE, PROJECT_ADMINS_MANAGEMENT,
			PROJECT_MEMBER_MANAGEMENT
		)),
		MEMBER(List.of(
			AUTHENTICATED, PROFILE, PROJECT_READ
		));

		private final List<Capability> capabilities;
		private final static String roleName = "furmsProjectRole";

		PROJECT_ROLE(List<Capability> capabilities){
			this.capabilities = capabilities;
		}

		@Override
		public List<Capability> getCapabilities() {
			return capabilities;
		}
	}

	public static FurmsRole translateRole(String attributeType, String value){
		return ofNullable(roleTranslator.get(attributeType)).stream()
			.flatMap(Arrays::stream)
			.filter(r -> r.name().equals(value))
			.findAny()
			.orElseThrow(() -> new RuntimeException("Invalid attributeType: " + attributeType + " or value: " + value));
	}

}
