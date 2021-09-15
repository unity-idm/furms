/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.invitations;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationAcceptedEvent;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.invitations.RemoveInvitationUserEvent;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.AddUserEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.invitations.InvitationRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteGroupDAO;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

class InvitationServiceImplTest {

	@Mock
	private InvitationRepository invitationRepository;
	@Mock
	private AuthzService authzService;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private SiteGroupDAO siteGroupDAO;
	@Mock
	private CommunityGroupsDAO communityGroupsDAO;
	@Mock
	private ProjectGroupsDAO projectGroupsDAO;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private ApplicationEventPublisher publisher;

	private InvitationServiceImpl invitationService;

	private InOrder orderVerifier;


	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		invitationService = new InvitationServiceImpl(
			invitationRepository, authzService, usersDAO, siteGroupDAO, communityGroupsDAO, projectGroupsDAO,
			projectRepository, publisher
		);
		orderVerifier = inOrder(invitationRepository, usersDAO, siteGroupDAO, communityGroupsDAO, projectGroupsDAO, publisher);
	}

	@Test
	void shouldAcceptInvitationForAppLevel() {
		InvitationId invitationId = new InvitationId(UUID.randomUUID());
		FenixUserId userId = new FenixUserId("userId");
		Invitation invitation = Invitation.builder()
			.id(invitationId)
			.resourceId(new ResourceId((UUID) null, APP_LEVEL))
			.resourceName("resourceName")
			.originator("originator")
			.userId(userId)
			.email("email")
			.role(Role.FENIX_ADMIN)
			.code("code")
			.utcExpiredAt(LocalDate.now().atStartOfDay())
			.build();
		PersistentId persistentId = new PersistentId("id");
		FURMSUser user = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(userId)
			.email("email")
			.build();

		when(authzService.getCurrentAuthNUser()).thenReturn(user);
		when(invitationRepository.findBy(invitationId)).thenReturn(Optional.of(invitation));

		invitationService.acceptBy(invitationId);

		orderVerifier.verify(usersDAO).addFenixAdminRole(persistentId);
		orderVerifier.verify(invitationRepository).deleteBy(invitationId);
		orderVerifier.verify(publisher).publishEvent(new InvitationAcceptedEvent(userId, invitation.resourceId));
		orderVerifier.verify(publisher).publishEvent(new AddUserEvent(persistentId, invitation.resourceId));
	}

	@Test
	void shouldAcceptInvitationForSiteLevel() {
		InvitationId invitationId = new InvitationId(UUID.randomUUID());
		FenixUserId userId = new FenixUserId("userId");
		ResourceId resourceId = new ResourceId(UUID.randomUUID(), SITE);
		Invitation invitation = Invitation.builder()
			.id(invitationId)
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId(userId)
			.email("email")
			.role(Role.SITE_ADMIN)
			.code("code")
			.utcExpiredAt(LocalDate.now().atStartOfDay())
			.build();
		PersistentId persistentId = new PersistentId("id");
		FURMSUser user = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(userId)
			.email("email")
			.build();

		when(authzService.getCurrentAuthNUser()).thenReturn(user);
		when(invitationRepository.findBy(invitationId)).thenReturn(Optional.of(invitation));

		invitationService.acceptBy(invitationId);

		orderVerifier.verify(siteGroupDAO).addSiteUser(resourceId.id.toString(), persistentId, Role.SITE_ADMIN);
		orderVerifier.verify(invitationRepository).deleteBy(invitationId);
		orderVerifier.verify(publisher).publishEvent(new InvitationAcceptedEvent(userId, invitation.resourceId));
		orderVerifier.verify(publisher).publishEvent(new AddUserEvent(persistentId, invitation.resourceId));
	}

	@Test
	void shouldAcceptInvitationForCommunityLevel() {
		InvitationId invitationId = new InvitationId(UUID.randomUUID());
		FenixUserId userId = new FenixUserId("userId");
		ResourceId resourceId = new ResourceId(UUID.randomUUID(), COMMUNITY);
		Invitation invitation = Invitation.builder()
			.id(invitationId)
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId(userId)
			.email("email")
			.role(Role.COMMUNITY_ADMIN)
			.code("code")
			.utcExpiredAt(LocalDate.now().atStartOfDay())
			.build();
		PersistentId persistentId = new PersistentId("id");
		FURMSUser user = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(userId)
			.email("email")
			.build();

		when(authzService.getCurrentAuthNUser()).thenReturn(user);
		when(invitationRepository.findBy(invitationId)).thenReturn(Optional.of(invitation));

		invitationService.acceptBy(invitationId);

		orderVerifier.verify(communityGroupsDAO).addAdmin(resourceId.id.toString(), persistentId);
		orderVerifier.verify(invitationRepository).deleteBy(invitationId);
		orderVerifier.verify(publisher).publishEvent(new InvitationAcceptedEvent(userId, invitation.resourceId));
		orderVerifier.verify(publisher).publishEvent(new AddUserEvent(persistentId, invitation.resourceId));
	}

	@Test
	void shouldAcceptInvitationForProjectLevel() {
		InvitationId invitationId = new InvitationId(UUID.randomUUID());
		FenixUserId userId = new FenixUserId("userId");
		ResourceId resourceId = new ResourceId(UUID.randomUUID(), PROJECT);
		Invitation invitation = Invitation.builder()
			.id(invitationId)
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId(userId)
			.email("email")
			.role(Role.PROJECT_ADMIN)
			.code("code")
			.utcExpiredAt(LocalDate.now().atStartOfDay())
			.build();
		PersistentId persistentId = new PersistentId("id");
		FURMSUser user = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(userId)
			.email("email")
			.build();

		when(authzService.getCurrentAuthNUser()).thenReturn(user);
		when(invitationRepository.findBy(invitationId)).thenReturn(Optional.of(invitation));
		when(projectRepository.findById(resourceId.id.toString())).thenReturn(Optional.of(Project.builder()
			.communityId("communityId")
			.build()));

		invitationService.acceptBy(invitationId);

		orderVerifier.verify(projectGroupsDAO).addProjectUser("communityId", resourceId.id.toString(), persistentId, Role.PROJECT_ADMIN);
		orderVerifier.verify(invitationRepository).deleteBy(invitationId);
		orderVerifier.verify(publisher).publishEvent(new InvitationAcceptedEvent(userId, invitation.resourceId));
		orderVerifier.verify(publisher).publishEvent(new AddUserEvent(persistentId, invitation.resourceId));
	}

	@Test
	void shouldFindAllForCurrentUser() {
		PersistentId persistentId = new PersistentId("id");
		FenixUserId userId = new FenixUserId("userId");
		FURMSUser user = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(userId)
			.email("email")
			.build();
		when(authzService.getCurrentAuthNUser()).thenReturn(user);

		invitationService.findAllByCurrentUser();

		orderVerifier.verify(invitationRepository).findAllBy(userId, "email");
	}

	@Test
	void shouldDeleteById() {
		InvitationId invitationId = new InvitationId(UUID.randomUUID());
		PersistentId persistentId = new PersistentId("id");
		FenixUserId userId = new FenixUserId("userId");
		FURMSUser user = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(userId)
			.email("email")
			.build();
		Invitation invitation = Invitation.builder()
			.id(invitationId)
			.userId(userId)
			.build();

		when(authzService.getCurrentAuthNUser()).thenReturn(user);
		when(invitationRepository.findBy(invitationId, userId)).thenReturn(Optional.of(invitation));

		invitationService.acceptInvitationByRegistration(invitationId);

		orderVerifier.verify(invitationRepository).deleteBy(invitationId);
		orderVerifier.verify(publisher).publishEvent(new RemoveInvitationUserEvent(user.fenixUserId.get(), invitationId, InvitationCode.empty()));
	}

	@Test
	void shouldAcceptInvitationByRegistration() {
		InvitationId invitationId = new InvitationId(UUID.randomUUID());
		InvitationCode invitationCode = new InvitationCode("code");
		PersistentId persistentId = new PersistentId("id");
		FenixUserId userId = new FenixUserId("userId");
		FURMSUser user = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(userId)
			.email("email")
			.build();
		Invitation invitation = Invitation.builder()
			.id(invitationId)
			.email("email")
			.code(invitationCode)
			.build();

		when(usersDAO.getAllUsers()).thenReturn(List.of(user));
		when(usersDAO.findByRegistrationId("registrationId")).thenReturn(invitationCode);
		when(invitationRepository.findBy(invitationCode)).thenReturn(Optional.of(invitation));
		when(invitationRepository.findAllBy(FenixUserId.empty(), "email")).thenReturn(Set.of(invitation));

		invitationService.acceptInvitationByRegistration("registrationId");

		orderVerifier.verify(invitationRepository).deleteBy(invitationCode);
		orderVerifier.verify(invitationRepository).updateUserId(invitation, userId);
	}
}