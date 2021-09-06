/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.invitations;


import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface InvitationRepository {

	Optional<Invitation> findBy(InvitationId id, FenixUserId userId);

	Set<Invitation> findAllBy(FenixUserId id, String email);

	Set<Invitation> findAllBy(Role role, UUID resourceId);

	void create(Invitation invitation);

	void deleteBy(InvitationId id);

	void deleteBy(InvitationCode unityCode);

	void deleteAll();
}

