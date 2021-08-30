/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.invitations;

import io.imunity.furms.domain.authz.roles.Role;

import java.time.LocalDateTime;

class InvitationGridModel {
	String id;
	String resourceName;
	Role role;
	String originator;
	LocalDateTime expiration;
}
