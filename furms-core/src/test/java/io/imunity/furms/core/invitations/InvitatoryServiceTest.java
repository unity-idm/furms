/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.invitations;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.validation.exceptions.DuplicatedInvitationError;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserAttributes;
import io.imunity.furms.spi.invitations.InvitationRepository;
import io.imunity.furms.spi.notifications.NotificationDAO;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvitatoryServiceTest {

	private final static Instant LOCAL_DATE = Instant.now();
	private final static int EXPIRATION_TIME = 1000;

	@Mock
	private UsersDAO usersDAO;
	@Mock
	private InvitationRepository invitationRepository;
	@Mock
	private AuthzService authzService;
	@Mock
	private NotificationDAO notificationDAO;
	@Mock
	private ApplicationEventPublisher publisher;

	private InvitatoryService invitatoryService;
	private Clock fixedClock;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		fixedClock = Clock.fixed(LOCAL_DATE, ZoneId.systemDefault());
		invitatoryService = new InvitatoryService(usersDAO, invitationRepository, authzService,
			notificationDAO, publisher, fixedClock, String.valueOf(EXPIRATION_TIME));
	}

	@Test
	void shouldGetInvitations() {
		UUID id = UUID.randomUUID();
		Role role = Role.FENIX_ADMIN;
		Invitation invitation = Invitation.builder()
			.id(new InvitationId(UUID.randomUUID()))
			.build();

		when(invitationRepository.findAllBy(role, id)).thenReturn(Set.of(invitation));

		Set<Invitation> invitations = invitatoryService.getInvitations(role, id);

		assertEquals(Set.of(invitation), invitations);
	}

	@Test
	void shouldInviteExistingUser() {
		Role role = Role.FENIX_ADMIN;
		ResourceId resourceId = new ResourceId((UUID) null, ResourceType.APP_LEVEL);
		PersistentId persistentId = new PersistentId("id");
		FenixUserId fenixId = new FenixUserId("fenixId");
		FURMSUser furmsUser = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(fenixId)
			.email("email")
			.build();
		Invitation invitation = Invitation.builder()
			.resourceId(resourceId)
			.role(role)
			.userId(furmsUser.fenixUserId.get())
			.originator("originator")
			.email(furmsUser.email)
			.utcExpiredAt(getExpiredAt())
			.build();

		when(usersDAO.findById(persistentId)).thenReturn(Optional.of(furmsUser));
		when(usersDAO.getUserAttributes(fenixId)).thenReturn(new UserAttributes(Set.of(), Map.of()));
		when(invitationRepository.findBy("email", role, resourceId)).thenReturn(Optional.empty());
		when(authzService.getCurrentAuthNUser()).thenReturn(FURMSUser.builder()
			.email("originator")
			.build()
		);

		invitatoryService.inviteUser(persistentId, resourceId, role);

		verify(invitationRepository).create(invitation);
		verify(notificationDAO).notifyUser(persistentId, invitation);
	}

	@Test
	void shouldNotInviteExistingUserWhenInvitationAlreadyExists() {
		Role role = Role.FENIX_ADMIN;
		ResourceId resourceId = new ResourceId((UUID) null, ResourceType.APP_LEVEL);
		PersistentId persistentId = new PersistentId("id");
		FURMSUser furmsUser = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(new FenixUserId("fenixId"))
			.email("email")
			.build();
		Invitation invitation = Invitation.builder()
			.resourceId(resourceId)
			.role(role)
			.userId(furmsUser.fenixUserId.get())
			.originator("originator")
			.email(furmsUser.email)
			.utcExpiredAt(getExpiredAt())
			.build();

		when(usersDAO.findById(persistentId)).thenReturn(Optional.of(furmsUser));
		when(invitationRepository.findBy("email", role, resourceId)).thenReturn(Optional.of(invitation));
		when(authzService.getCurrentAuthNUser()).thenReturn(FURMSUser.builder()
			.email("originator")
			.build()
		);

		assertThrows(DuplicatedInvitationError.class, () -> invitatoryService.inviteUser(persistentId, resourceId, role));

	}

	@Test
	void shouldDetectAndInviteExistingUser() {
		Role role = Role.FENIX_ADMIN;
		ResourceId resourceId = new ResourceId((UUID) null, ResourceType.APP_LEVEL);
		PersistentId persistentId = new PersistentId("id");
		FenixUserId fenixId = new FenixUserId("fenixId");
		FURMSUser furmsUser = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(fenixId)
			.email("email")
			.build();
		Invitation invitation = Invitation.builder()
			.resourceId(resourceId)
			.role(role)
			.userId(furmsUser.fenixUserId.get())
			.originator("originator")
			.email(furmsUser.email)
			.utcExpiredAt(getExpiredAt())
			.build();

		when(usersDAO.findById(persistentId)).thenReturn(Optional.of(furmsUser));
		when(usersDAO.getAllUsers()).thenReturn(List.of(furmsUser));
		when(usersDAO.getUserAttributes(fenixId)).thenReturn(new UserAttributes(Set.of(), Map.of()));
		when(invitationRepository.findBy("email", role, resourceId)).thenReturn(Optional.empty());
		when(authzService.getCurrentAuthNUser()).thenReturn(FURMSUser.builder()
			.email("originator")
			.build()
		);

		invitatoryService.inviteUser("email", resourceId, role);

		verify(invitationRepository).create(invitation);
		verify(notificationDAO).notifyUser(persistentId, invitation);
	}

	@Test
	void shouldInviteNewUser() {
		Role role = Role.FENIX_ADMIN;
		ResourceId resourceId = new ResourceId((UUID) null, ResourceType.APP_LEVEL);
		PersistentId persistentId = new PersistentId("id");
		InvitationCode code = new InvitationCode("code");
		FURMSUser furmsUser = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(new FenixUserId("fenixId"))
			.email("email")
			.build();
		Invitation invitation = Invitation.builder()
			.resourceId(resourceId)
			.role(role)
			.code(code)
			.originator("originator")
			.email(furmsUser.email)
			.utcExpiredAt(getExpiredAt())
			.build();

		when(usersDAO.inviteUser("email", getExpiredAt().toInstant(ZoneOffset.UTC), role)).thenReturn(code);
		when(usersDAO.findById(persistentId)).thenReturn(Optional.of(furmsUser));
		when(invitationRepository.findBy("email", role, resourceId)).thenReturn(Optional.empty());
		when(authzService.getCurrentAuthNUser()).thenReturn(FURMSUser.builder()
			.email("originator")
			.build()
		);

		invitatoryService.inviteUser("email", resourceId, role);

		verify(invitationRepository).create(invitation);
		verify(usersDAO).inviteUser("email", getExpiredAt().toInstant(ZoneOffset.UTC), role);
	}

	@Test
	void shouldNotInviteNewUserWhenInvitationAlreadyExists() {
		Role role = Role.FENIX_ADMIN;
		ResourceId resourceId = new ResourceId((UUID) null, ResourceType.APP_LEVEL);
		PersistentId persistentId = new PersistentId("id");
		FURMSUser furmsUser = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(new FenixUserId("fenixId"))
			.email("email")
			.build();
		Invitation invitation = Invitation.builder()
			.resourceId(resourceId)
			.role(role)
			.originator("originator")
			.email(furmsUser.email)
			.utcExpiredAt(getExpiredAt())
			.build();

		when(usersDAO.findById(persistentId)).thenReturn(Optional.of(furmsUser));
		when(invitationRepository.findBy("email", role, resourceId)).thenReturn(Optional.of(invitation));
		when(authzService.getCurrentAuthNUser()).thenReturn(FURMSUser.builder()
			.email("originator")
			.build()
		);

		assertThrows(DuplicatedInvitationError.class, () -> invitatoryService.inviteUser("email", resourceId, role));
	}

	@Test
	void shouldRemoveInvitationWhenCreatingFailed() {
		Role role = Role.FENIX_ADMIN;
		ResourceId resourceId = new ResourceId((UUID) null, ResourceType.APP_LEVEL);
		PersistentId persistentId = new PersistentId("id");
		InvitationCode invitationCode = new InvitationCode("code");

		FURMSUser furmsUser = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(new FenixUserId("fenixId"))
			.email("email")
			.build();
		Invitation invitation = Invitation.builder()
			.resourceId(resourceId)
			.role(role)
			.code(invitationCode)
			.originator("originator")
			.email(furmsUser.email)
			.utcExpiredAt(getExpiredAt())
			.build();

		when(usersDAO.findById(persistentId)).thenReturn(Optional.of(furmsUser));
		when(usersDAO.inviteUser("email", getExpiredAt().toInstant(ZoneOffset.UTC), role)).thenReturn(invitationCode);
		when(invitationRepository.findBy("email", role, resourceId)).thenReturn(Optional.empty());
		when(authzService.getCurrentAuthNUser()).thenReturn(FURMSUser.builder()
			.email("originator")
			.build()
		);
		when(invitationRepository.create(invitation)).thenThrow(new RuntimeException());

		assertThrows(RuntimeException.class, () -> invitatoryService.inviteUser("email", resourceId, role));

		verify(usersDAO).removeInvitation(invitationCode);
	}

	private LocalDateTime getExpiredAt() {
		return ZonedDateTime.now(fixedClock).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().plusSeconds(EXPIRATION_TIME);
	}

	@Test
	void shouldResendInvitationForNewUser() {
		Role role = Role.FENIX_ADMIN;
		ResourceId resourceId = new ResourceId((UUID) null, ResourceType.APP_LEVEL);
		InvitationCode invitationCode = new InvitationCode("code");
		InvitationId invitationId = new InvitationId(UUID.randomUUID());

		Invitation invitation = Invitation.builder()
			.id(invitationId)
			.resourceId(resourceId)
			.role(role)
			.code(invitationCode)
			.originator("originator")
			.email("email")
			.utcExpiredAt(getExpiredAt())
			.build();

		when(invitationRepository.findBy(invitationId)).thenReturn(Optional.of(invitation));

		invitatoryService.resendInvitation(invitationId);

		verify(usersDAO).resendInvitation("email", invitationCode, getExpiredAt().toInstant(ZoneOffset.UTC), role);
	}

	@Test
	void shouldResendInvitationForExistingUser() {
		Role role = Role.FENIX_ADMIN;
		ResourceId resourceId = new ResourceId((UUID) null, ResourceType.APP_LEVEL);
		InvitationId invitationId = new InvitationId(UUID.randomUUID());
		PersistentId persistentId = new PersistentId("id");
		FenixUserId fenixId = new FenixUserId("fenixId");

		FURMSUser furmsUser = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(fenixId)
			.email("email")
			.build();
		Invitation invitation = Invitation.builder()
			.id(invitationId)
			.resourceId(resourceId)
			.userId(fenixId)
			.role(role)
			.originator("originator")
			.email("email")
			.utcExpiredAt(getExpiredAt())
			.build();

		when(invitationRepository.findBy(invitationId)).thenReturn(Optional.of(invitation));
		when(usersDAO.getAllUsers()).thenReturn(List.of(furmsUser));

		invitatoryService.resendInvitation(invitationId);

		verify(notificationDAO).notifyUser(persistentId, invitation);
	}

	@Test
	void removeInvitationForExistingUser() {
		Role role = Role.FENIX_ADMIN;
		ResourceId resourceId = new ResourceId((UUID) null, ResourceType.APP_LEVEL);
		InvitationId invitationId = new InvitationId(UUID.randomUUID());

		Invitation invitation = Invitation.builder()
			.id(invitationId)
			.resourceId(resourceId)
			.role(role)
			.originator("originator")
			.email("email")
			.utcExpiredAt(getExpiredAt())
			.build();

		when(invitationRepository.findBy(invitationId)).thenReturn(Optional.of(invitation));

		invitatoryService.removeInvitation(invitationId);

		verify(invitationRepository).deleteBy(invitationId);
	}

	@Test
	void removeInvitationForNewUser() {
		Role role = Role.FENIX_ADMIN;
		ResourceId resourceId = new ResourceId((UUID) null, ResourceType.APP_LEVEL);
		InvitationCode invitationCode = new InvitationCode("code");
		InvitationId invitationId = new InvitationId(UUID.randomUUID());

		Invitation invitation = Invitation.builder()
			.id(invitationId)
			.resourceId(resourceId)
			.role(role)
			.code(invitationCode)
			.originator("originator")
			.email("email")
			.utcExpiredAt(getExpiredAt())
			.build();

		when(invitationRepository.findBy(invitationId)).thenReturn(Optional.of(invitation));

		invitatoryService.removeInvitation(invitationId);

		verify(invitationRepository).deleteBy(invitationId);
		verify(usersDAO).removeInvitation(invitationCode);
	}

}