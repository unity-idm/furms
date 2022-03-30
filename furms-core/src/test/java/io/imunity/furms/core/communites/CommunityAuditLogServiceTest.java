/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.core.audit_log.AuditLogPackageTestExposer;
import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.core.users.audit_log.RoleAssignmentAuditLogServiceTest;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackageClasses = {CommunityAuditLogService.class,
	RoleAssignmentAuditLogServiceTest.class, AuditLogPackageTestExposer.class})
class CommunityAuditLogServiceTest {
	@MockBean
	private CommunityRepository communityRepository;
	@MockBean
	private CommunityGroupsDAO communityGroupsDAO;
	@MockBean
	private ProjectRepository projectRepository;
	@MockBean
	private AuthzService authzService;
	@MockBean
	private CapabilityCollector capabilityCollector;
	@MockBean
	private InvitatoryService invitatoryService;

	@MockBean
	private UsersDAO usersDAO;
	@MockBean
	private ObjectMapper objectMapper;
	@Autowired
	private ApplicationEventPublisher publisher;
	@MockBean
	private AuditLogRepository auditLogRepository;

	private CommunityServiceImpl service;

	@BeforeEach
	void setUp() {
		CommunityServiceValidator validator = new CommunityServiceValidator(communityRepository, projectRepository);
		service = new CommunityServiceImpl(communityRepository, communityGroupsDAO, validator, authzService,
			publisher, capabilityCollector, invitatoryService);
	}

	@Test
	void shouldDetectCommunityDeletion() {
		//given
		String id = "id";
		when(communityRepository.exists(id)).thenReturn(true);
		Community community = Community.builder().build();
		when(communityRepository.findById(id)).thenReturn(Optional.of(community));

		//when
		service.delete(id);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.COMMUNITIES_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.DELETE, argument.getValue().action);
	}

	@Test
	void shouldDetectCommunityUpdate() {
		//given
		Community request = Community.builder()
			.id("id")
			.name("userFacingName")
			.build();
		when(communityRepository.exists(request.getId())).thenReturn(true);
		when(communityRepository.isUniqueName(request.getName())).thenReturn(true);
		when(communityRepository.findById("id")).thenReturn(Optional.of(request));

		//when
		service.update(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.COMMUNITIES_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.UPDATE, argument.getValue().action);
	}

	@Test
	void shouldDetectCommunityCreation() {
		//given
		Community request = Community.builder()
			.id("id")
			.name("userFacingName")
			.build();
		when(communityRepository.isUniqueName(request.getName())).thenReturn(true);
		when(communityRepository.findById("id")).thenReturn(Optional.of(request));
		when(communityRepository.create(request)).thenReturn("id");

		//when
		service.create(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.COMMUNITIES_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.CREATE, argument.getValue().action);
	}

	@Test
	void shouldDetectAdminAddition() {
		//given
		String communityId = UUID.randomUUID().toString();
		PersistentId userId = new PersistentId("userId");
		Community community = Community.builder().build();
		when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
			.id(userId)
			.email("email")
			.build())
		);
		//when
		service.addAdmin(communityId, userId);

		//then
		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.ROLE_ASSIGNMENT, argument.getValue().operationCategory);
		assertEquals(Action.GRANT, argument.getValue().action);
	}

	@Test
	void shouldDetectAdminRemoval() {
		//given
		String communityId = UUID.randomUUID().toString();
		PersistentId userId = new PersistentId("userId");
		Community community = Community.builder()
			.name("name")
			.build();
		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
			.id(userId)
			.email("email")
			.build())
		);
		when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

		//when
		service.removeAdmin(communityId, userId);

		//then
		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.ROLE_ASSIGNMENT, argument.getValue().operationCategory);
		assertEquals(Action.REVOKE, argument.getValue().action);
	}
}
