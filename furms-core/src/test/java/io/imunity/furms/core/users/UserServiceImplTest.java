/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.InviteUserEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@InjectMocks
	private UserServiceImpl service;

	@Mock
	private UsersDAO usersDAO;
	@Mock
	private ApplicationEventPublisher publisher;

	@Test
	void shouldAllowToInviteUser() {
		//given
		PersistentId id = new PersistentId("userId");
		when(usersDAO.findById(eq(id))).thenReturn(Optional.of(FURMSUser.builder()
				.id(id)
				.email("email")
				.build()));

		//when
		service.inviteFenixAdmin(id);

		//then
		verify(usersDAO, times(1)).addFenixAdminRole(eq(id));
		verify(publisher, times(1)).publishEvent(new InviteUserEvent(id, new ResourceId((String) null, APP_LEVEL)));
	}
}