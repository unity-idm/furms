/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.authz.roles;

public enum Capability {
	USERS_MAINTENANCE,
	REST_API_KEY_MANAGEMENT,
	AUTHENTICATED,
	PROFILE, 
	SITE_READ, 
	SITE_WRITE,
	SITE_POLICY_ACCEPTANCE_READ,
	SITE_POLICY_ACCEPTANCE_WRITE,
	COMMUNITY_READ,
	COMMUNITY_WRITE,
	MEMBERSHIP_GROUP_READ,
	MEMBERSHIP_GROUP_WRITE,
	FENIX_ADMINS_MANAGEMENT, 
	PROJECT_READ, 
	PROJECT_WRITE, 
	PROJECT_LIMITED_READ, 
	PROJECT_LIMITED_WRITE, 
	PROJECT_MEMBER_MANAGEMENT,
	PROJECT_ADMINS_MANAGEMENT, 
	READ_ALL_USERS, 
	PROJECT_LEAVE, 
	OWNED_SSH_KEY_MANAGMENT,
	POLICY_ACCEPTANCE_MAINTENANCE
}
