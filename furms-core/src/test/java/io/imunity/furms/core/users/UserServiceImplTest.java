/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import io.imunity.furms.domain.users.User;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
		String email = "email";
		when(usersDAO.findByEmail(eq(email))).thenReturn(Optional.of(User.builder()
				.id("userId")
				.email(email)
				.build()));

		//when
		service.inviteFenixAdmin(email);

		//then
		verify(usersDAO, times(1)).addFenixAdminRole(eq("userId"));
	}

	@Test
	void shouldNotAllowToInviteUserDueToLackOfUserWithThisEmail() {
		//given
		String email = "email";
		when(usersDAO.findByEmail(eq(email))).thenReturn(Optional.empty());

		//when
		assertThrows(IllegalArgumentException.class, () -> service.inviteFenixAdmin(email));
	}

}