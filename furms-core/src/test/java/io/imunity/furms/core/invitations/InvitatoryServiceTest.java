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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
	void shouldFindInvitationAssociationWithResourceId() {
		ResourceId resourceId = new ResourceId(UUID.randomUUID(), ResourceType.SITE);
		Invitation invitation = Invitation.builder()
			.id(new InvitationId(UUID.randomUUID()))
			.resourceId(resourceId)
			.build();

		when(invitationRepository.findBy(invitation.id)).thenReturn(Optional.of(invitation));

		boolean associated = invitatoryService.checkAssociation(resourceId.id.toString(), invitation.id);

		assertTrue(associated);
	}

	@Test
	void shouldNotFindInvitationAssociationWithResourceId() {
		ResourceId resourceId = new ResourceId(UUID.randomUUID(), ResourceType.SITE);
		Invitation invitation = Invitation.builder()
			.id(new InvitationId(UUID.randomUUID()))
			.resourceId(resourceId)
			.build();

		when(invitationRepository.findBy(invitation.id)).thenReturn(Optional.of(invitation));

		boolean associated = invitatoryService.checkAssociation(UUID.randomUUID().toString(), invitation.id);

		assertFalse(associated);
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
			.resourceName("system")
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

		invitatoryService.inviteUser(persistentId, resourceId, role, "system");

		verify(invitationRepository).create(invitation);
		verify(notificationDAO).notifyUserAboutNewRole(persistentId, invitation.role);
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

		assertThrows(DuplicatedInvitationError.class, () -> invitatoryService.inviteUser(persistentId, resourceId, role, "system"));

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
			.resourceName("system")
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

		invitatoryService.inviteUser("email", resourceId, role, "system");

		verify(invitationRepository).create(invitation);
		verify(notificationDAO).notifyUserAboutNewRole(persistentId, invitation.role);
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
			.resourceName("system")
			.originator("originator")
			.email(furmsUser.email)
			.utcExpiredAt(getExpiredAt())
			.build();

		when(usersDAO.inviteUser(resourceId, role, "email", getExpiredAt().toInstant(ZoneOffset.UTC))).thenReturn(code);
		when(usersDAO.findById(persistentId)).thenReturn(Optional.of(furmsUser));
		when(invitationRepository.findBy("email", role, resourceId)).thenReturn(Optional.empty());
		when(authzService.getCurrentAuthNUser()).thenReturn(FURMSUser.builder()
			.email("originator")
			.build()
		);

		invitatoryService.inviteUser("email", resourceId, role, "system");

		verify(invitationRepository).create(invitation);
		verify(usersDAO).inviteUser(resourceId, role,"email", getExpiredAt().toInstant(ZoneOffset.UTC));
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

		assertThrows(DuplicatedInvitationError.class, () -> invitatoryService.inviteUser("email", resourceId, role, "system"));
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
			.resourceName("system")
			.code(invitationCode)
			.originator("originator")
			.email(furmsUser.email)
			.utcExpiredAt(getExpiredAt())
			.build();

		when(usersDAO.findById(persistentId)).thenReturn(Optional.of(furmsUser));
		when(usersDAO.inviteUser(resourceId, role,"email", getExpiredAt().toInstant(ZoneOffset.UTC))).thenReturn(invitationCode);
		when(invitationRepository.findBy("email", role, resourceId)).thenReturn(Optional.empty());
		when(authzService.getCurrentAuthNUser()).thenReturn(FURMSUser.builder()
			.email("originator")
			.build()
		);
		when(invitationRepository.create(invitation)).thenThrow(new RuntimeException());

		assertThrows(RuntimeException.class, () -> invitatoryService.inviteUser("email", resourceId, role, "system"));

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

		verify(usersDAO).resendInvitation(invitation, getExpiredAt().toInstant(ZoneOffset.UTC));
	}

	@Test
	void shouldUpdateInvitationRoleForNewUser() {
		Role role = Role.SITE_SUPPORT;
		ResourceId resourceId = new ResourceId(UUID.randomUUID(), ResourceType.SITE);
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

		invitatoryService.updateInvitationRole(invitationId, Role.SITE_ADMIN);

		verify(invitationRepository).updateExpiredAtAndRole(invitationId, getExpiredAt(), Role.SITE_ADMIN);
		verify(usersDAO).resendInvitation(invitation, getExpiredAt().toInstant(ZoneOffset.UTC), Role.SITE_ADMIN);
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

		verify(notificationDAO).notifyUserAboutNewRole(persistentId, invitation.role);
	}

	@Test
	void shouldUpdateInvitationRoleForExistingUser() {
		Role role = Role.SITE_ADMIN;
		ResourceId resourceId = new ResourceId(UUID.randomUUID(), ResourceType.SITE);
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

		invitatoryService.updateInvitationRole(invitationId, Role.SITE_SUPPORT);

		verify(invitationRepository).updateExpiredAtAndRole(invitationId, getExpiredAt(), Role.SITE_SUPPORT);
		verify(notificationDAO).notifyUserAboutNewRole(persistentId, Role.SITE_SUPPORT);
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