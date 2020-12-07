/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config;

import java.util.List;

public interface Role {;
	List<String> getCapabilities();

	enum FENIX_ROLE implements Role{
		ADMIN(List.of(
			"AUTHENTICATED", "PROFILE", "SITE_READ", "SITE_WRITE", "COMMUNITY_READ", "COMMUNITY_WRITE",
			"FENIX_ADMINS_MANAGEMENT"
		)),
		SUPPORT(List.of(

		));

		private final List<String> capabilities;

		FENIX_ROLE(List<String> capabilities){
			this.capabilities = capabilities;
		}

		@Override
		public List<String> getCapabilities() {
			return capabilities;
		}
	}

	enum SITE_ROLE implements Role{
		ADMIN(List.of(
			"AUTHENTICATED", "PROFILE", "SITE_READ", "SITE_WRITE", "SITE_ADMINS_MANAGEMENT"
		));

		private final List<String> capabilities;

		SITE_ROLE(List<String> capabilities){
			this.capabilities = capabilities;
		}

		@Override
		public List<String> getCapabilities() {
			return capabilities;
		}
	}

	enum COMMUNITY_ROLE implements Role{
		ADMIN(List.of(
			"AUTHENTICATED", "PROFILE", "COMMUNITY_READ", "COMMUNITY_WRITE", "PROJECT_READ", "PROJECT_WRITE",
			"COMMUNITY_ADMINS_MANAGEMENT"
		));

		private final List<String> capabilities;

		COMMUNITY_ROLE(List<String> capabilities){
			this.capabilities = capabilities;
		}

		@Override
		public List<String> getCapabilities() {
			return capabilities;
		}
	}

	enum PROJECT_ROLE implements Role{
		ADMIN(List.of(
			"AUTHENTICATED", "PROFILE", "PROJECT_READ", "PROJECT_WRITE", "PROJECT_ADMINS_MANAGEMENT",
			"PROJECT_MEMBER_MANAGEMENT"
		)),
		MEMBER(List.of(
			"AUTHENTICATED", "PROFILE", "PROJECT_READ"
		));

		private final List<String> capabilities;

		PROJECT_ROLE(List<String> capabilities){
			this.capabilities = capabilities;
		}

		@Override
		public List<String> getCapabilities() {
			return capabilities;
		}
	}

	static Role translateRole(String attributeType, String value){
		switch(attributeType) {
			case "furmsSiteRole":
				return SITE_ROLE.valueOf(value);
			case "furmsFenixRole":
				return FENIX_ROLE.valueOf(value);
			case "furmsCommunityRole":
				return COMMUNITY_ROLE.valueOf(value);
			case "furmsProjectRole":
				return PROJECT_ROLE.valueOf(value);
			default: throw new UnsupportedOperationException();
		}
	}
}
