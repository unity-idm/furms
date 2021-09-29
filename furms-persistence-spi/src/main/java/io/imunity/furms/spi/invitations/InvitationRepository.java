/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.invitations;


import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.FenixUserId;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface InvitationRepository {

	Optional<Invitation> findBy(InvitationId id);

	Optional<Invitation> findBy(InvitationId id, String email);

	Optional<Invitation> findBy(InvitationCode code);

	Optional<Invitation> findBy(String email, Role role, ResourceId resourceId);

	Set<Invitation> findAllBy(FenixUserId id, String email);

	Set<Invitation> findAllBy(Role role, UUID resourceId);

	InvitationId create(Invitation invitation);

	void updateExpiredAt(InvitationId id, LocalDateTime utcExpiredAt);

	void updateExpiredAtAndRole(InvitationId id, LocalDateTime utcExpiredAt, Role role);

	void deleteBy(InvitationId id);

	void deleteBy(InvitationCode unityCode);

	void deleteAll();
}

