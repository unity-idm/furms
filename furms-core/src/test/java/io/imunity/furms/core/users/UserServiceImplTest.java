/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@InjectMocks
	private UserServiceImpl service;

	@Mock
	private UsersDAO usersDAO;

	@Test
	void shouldAllowToInviteUser() {
		//given
		String id = "id";
		when(usersDAO.findById(eq(id))).thenReturn(Optional.of(FURMSUser.builder()
				.id("userId")
				.email(id)
				.build()));

		//when
		service.inviteFenixAdmin(id);

		//then
		verify(usersDAO, times(1)).addFenixAdminRole(eq("userId"));
	}
}