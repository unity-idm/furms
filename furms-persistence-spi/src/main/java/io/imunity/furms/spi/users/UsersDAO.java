/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.users;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.users.AllUsersAndFenixAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserAttributes;
import io.imunity.furms.domain.users.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


public interface UsersDAO {
	List<FURMSUser> getAllUsers();
	InvitationCode inviteUser(ResourceId resourceId, Role role, String email, Instant invitationExpiration);
	InvitationCode findByRegistrationId(String registrationId);
	void removeInvitation(InvitationCode invitationCode);
	void resendInvitation(Invitation invitation, Instant invitationExpiration);
	void resendInvitation(Invitation invitation, Instant invitationExpiration, Role role);
	void setUserStatus(FenixUserId fenixUserId, UserStatus status);
	UserStatus getUserStatus(FenixUserId fenixUserId);
	Optional<FURMSUser> findById(PersistentId userId);
	Optional<FURMSUser> findById(FenixUserId userId);
	UserAttributes getUserAttributes(FenixUserId fenixUserId);
	AllUsersAndFenixAdmins getAllUsersAndFenixAdmins();
	PersistentId getPersistentId(FenixUserId userId);
	FenixUserId getFenixUserId(PersistentId userId);
}
