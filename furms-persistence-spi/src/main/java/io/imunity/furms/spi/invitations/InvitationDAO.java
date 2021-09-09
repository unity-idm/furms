/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.invitations;

import io.imunity.furms.domain.invitations.InvitationCode;

import java.time.Instant;

public interface InvitationDAO {
	String createInvitation(String email, Instant expiration);
	InvitationCode findInvitationCode(String registrationId);
	void sendInvitation(String code);
	void removeInvitation(String code);
}
