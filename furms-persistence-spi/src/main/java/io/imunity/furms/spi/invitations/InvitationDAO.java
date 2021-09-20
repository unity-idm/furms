/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.invitations;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.InvitationCode;

import java.time.Instant;

public interface InvitationDAO {
	InvitationCode createInvitation(ResourceId resourceId, Role role, String email, Instant expiration);
	void updateInvitation(ResourceId resourceId, Role role, String email, InvitationCode invitationCode, Instant expiration);
	InvitationCode findInvitationCode(String registrationId);
	void sendInvitation(InvitationCode code);
	void removeInvitation(InvitationCode code);
}
