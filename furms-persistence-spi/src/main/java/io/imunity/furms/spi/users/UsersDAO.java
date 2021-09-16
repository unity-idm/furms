/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.users;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.InvitationCode;
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
	InvitationCode inviteUser(String email, Instant invitationExpiration, Role role);
	InvitationCode findByRegistrationId(String registrationId);
	void removeInvitation(InvitationCode invitationCode);
	void resendInvitation(String email, InvitationCode invitationCode, Instant instant, Role role);
	void setUserStatus(FenixUserId fenixUserId, UserStatus status);
	UserStatus getUserStatus(FenixUserId fenixUserId);
	Optional<FURMSUser> findById(PersistentId userId);
	Optional<FURMSUser> findById(FenixUserId userId);
	UserAttributes getUserAttributes(FenixUserId fenixUserId);
	PersistentId getPersistentId(FenixUserId userId);
	FenixUserId getFenixUserId(PersistentId userId);
}
