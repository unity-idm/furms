/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.invitations;

import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationId;

import java.util.Set;

public interface InvitationService {
	void acceptBy(InvitationId id);
	Set<Invitation> findAllByCurrentUser();
	void acceptInvitationByRegistration(InvitationId id);
	void acceptInvitationByRegistration(String registrationId);
}
