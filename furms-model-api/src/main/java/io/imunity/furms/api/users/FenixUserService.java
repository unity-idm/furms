/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.users;

import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Set;

public interface FenixUserService {
	List<FURMSUser> getFenixAdmins();
	Set<Invitation> getFenixAdminsInvitations();
	void inviteFenixAdmin(String email);
	void inviteFenixAdmin(PersistentId userId);
	void resendFenixAdminInvitation(InvitationId id);
	void removeFenixAdminInvitation(InvitationId id);
	void removeFenixAdminRole(PersistentId userId);
}
