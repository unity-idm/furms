/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.users;

import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService {
	List<FURMSUser> getAllUsers();
	List<FURMSUser> getFenixAdmins();
	Set<Invitation> getFenixAdminsInvitations();
	void inviteFenixAdmin(String email);
	void inviteFenixAdmin(PersistentId userId);
	void resendFenixAdminInvitation(InvitationCode code);
	void removeFenixAdminInvitation(InvitationCode code);
	void removeFenixAdminRole(PersistentId userId);
	void setUserStatus(FenixUserId fenixUserId, UserStatus status);
	UserStatus getUserStatus(FenixUserId fenixUserId);
	Optional<FURMSUser> findById(PersistentId userId);
	Optional<FURMSUser> findByFenixUserId(FenixUserId fenixUserId);
	UserRecord getUserRecord(FenixUserId fenixUserId);
}
