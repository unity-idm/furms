/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.invitations;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.InvitationCode;

import java.time.Instant;

public interface InvitationDAO {
	InvitationCode createInvitation(String formId, String email, Instant expiration, Role role);
	void updateInvitation(String formId, String email, InvitationCode code, Instant expiration, Role role);
	InvitationCode findInvitationCode(String registrationId);
	void sendInvitation(InvitationCode code);
	void removeInvitation(InvitationCode code);
}
