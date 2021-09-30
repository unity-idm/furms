/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FenixUserServiceImplTest {

	@InjectMocks
	private FenixUserServiceImpl service;

	@Mock
	private UsersDAO usersDAO;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private InvitatoryService invitatoryService;

	@Test
	void shouldAllowToInviteUser() {
		// given
		PersistentId id = new PersistentId("userId");

		// when
		service.inviteFenixAdmin(id);

		// then
		verify(invitatoryService, times(1)).inviteUser(id, new ResourceId((UUID) null, APP_LEVEL), Role.FENIX_ADMIN, "system");
	}
}