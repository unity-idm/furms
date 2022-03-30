/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.projects;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.core.audit_log.AuditLogPackageTestExposer;
import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.core.project_installation.ProjectInstallationService;
import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.core.users.audit_log.RoleAssignmentAuditLogServiceTest;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackageClasses = {ProjectAuditLogService.class, AuditLogPackageTestExposer.class, RoleAssignmentAuditLogServiceTest.class})
class ProjectAuditLogServiceIntegrationTest {
	@MockBean
	private ProjectRepository projectRepository;
	@MockBean
	private CommunityRepository communityRepository;
	@MockBean
	private ProjectGroupsDAO projectGroupsDAO;
	@MockBean
	private UsersDAO usersDAO;
	@MockBean
	private ProjectInstallationService projectInstallationService;
	@MockBean
	private ProjectInstallationsService projectInstallationsService;
	@MockBean
	private UserOperationService userOperationService;
	@MockBean
	private CapabilityCollector capabilityCollector;
	@MockBean
	private InvitatoryService invitatoryService;

	@MockBean
	private AuthzService authzService;
	@MockBean
	private ObjectMapper objectMapper;
	@Autowired
	private ApplicationEventPublisher publisher;
	@MockBean
	private AuditLogRepository auditLogRepository;

	private ProjectServiceImpl service;

	@BeforeEach
	void init() {
		ProjectServiceValidator validator = new ProjectServiceValidator(projectRepository, communityRepository);
		service = new ProjectServiceImpl(
			projectRepository, projectGroupsDAO, usersDAO, validator,
			publisher, authzService, userOperationService, projectInstallationService,
			projectInstallationsService, capabilityCollector, invitatoryService);
	}

	@Test
	void shouldDetectProjectDeletion() {
		//given
		String id = "id";
		String id2 = "id";
		when(projectRepository.exists(id)).thenReturn(true);
		List<FURMSUser> users = Collections.singletonList(FURMSUser.builder().id(new PersistentId("id")).email("email@test.com").build());
		when(projectGroupsDAO.getAllUsers("id", "id")).thenReturn(users);
		Project project = Project.builder().build();
		when(projectRepository.findById("id")).thenReturn(Optional.of(project));

		//when
		service.delete(id, id2);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.PROJECTS_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.DELETE, argument.getValue().action);
	}

	@Test
	void shouldDetectProjectUpdate() {
		//given
		String id = UUID.randomUUID().toString();
		PersistentId projectLeaderId = new PersistentId(UUID.randomUUID().toString());
		Project request = Project.builder()
			.id(id)
			.communityId("id")
			.name("userFacingName")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.leaderId(projectLeaderId)
			.build();
		when(communityRepository.exists(request.getCommunityId())).thenReturn(true);
		when(projectRepository.exists(request.getId())).thenReturn(true);
		when(projectRepository.isNamePresent(request.getCommunityId(), request.getName())).thenReturn(true);
		when(projectRepository.findById(request.getId())).thenReturn(Optional.of(request));
		when(usersDAO.findById(projectLeaderId)).thenReturn(Optional.of(FURMSUser.builder()
			.id(projectLeaderId)
			.email("email")
			.build())
		);
		//when
		service.update(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository, times(2)).create(argument.capture());
		assertEquals(Operation.PROJECTS_MANAGEMENT, argument.getAllValues().get(1).operationCategory);
		assertEquals(Action.UPDATE, argument.getAllValues().get(1).action);
		assertEquals(Operation.ROLE_ASSIGNMENT, argument.getAllValues().get(0).operationCategory);
		assertEquals(Action.GRANT, argument.getAllValues().get(0).action);
	}

	@Test
	void shouldDetectProjectCreation() {
		//given
		String id = UUID.randomUUID().toString();
		PersistentId projectLeaderId = new PersistentId(UUID.randomUUID().toString());
		Project request = Project.builder()
			.id(id)
			.communityId("id")
			.name("userFacingName")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.leaderId(projectLeaderId)
			.build();
		when(communityRepository.exists("id")).thenReturn(true);
		when(projectRepository.isNamePresent(request.getCommunityId(), request.getName())).thenReturn(true);
		when(projectRepository.findById(id)).thenReturn(Optional.of(request));
		when(projectRepository.create(request)).thenReturn(id);
		when(usersDAO.findById(projectLeaderId)).thenReturn(Optional.of(FURMSUser.builder()
			.id(projectLeaderId)
			.email("email")
			.build())
		);
		//when
		service.create(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository, times(2)).create(argument.capture());
		assertEquals(Operation.PROJECTS_MANAGEMENT, argument.getAllValues().get(1).operationCategory);
		assertEquals(Action.CREATE, argument.getAllValues().get(1).action);
		assertEquals(Operation.ROLE_ASSIGNMENT, argument.getAllValues().get(0).operationCategory);
		assertEquals(Action.GRANT, argument.getAllValues().get(0).action);
	}

	@Test
	void shouldDetectAdminAddition() {
		UUID communityId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		PersistentId id = new PersistentId("id");
		Project project = Project.builder()
			.name("userFacingName")
			.build();
		when(projectRepository.findById(projectId.toString())).thenReturn(Optional.of(project));
		when(usersDAO.findById(id)).thenReturn(Optional.of(FURMSUser.builder()
			.id(id)
			.email("email")
			.build())
		);
		service.addAdmin(communityId.toString(), projectId.toString(), id);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.ROLE_ASSIGNMENT, argument.getValue().operationCategory);
		assertEquals(Action.GRANT, argument.getValue().action);
	}

	@Test
	void shouldDetectAdminRemoval() {
		UUID communityId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		PersistentId id = new PersistentId("id");
		Project project = Project.builder()
			.name("userFacingName")
			.build();
		when(projectRepository.findById(projectId.toString())).thenReturn(Optional.of(project));
		when(usersDAO.findById(id)).thenReturn(Optional.of(FURMSUser.builder()
			.id(id)
			.email("email")
			.build())
		);
		service.removeAdmin(communityId.toString(), projectId.toString(), id);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.ROLE_ASSIGNMENT, argument.getValue().operationCategory);
		assertEquals(Action.REVOKE, argument.getValue().action);
	}

	@Test
	void shouldDetectUserAddition() {
		UUID communityId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		PersistentId id = new PersistentId("id");
		Project project = Project.builder()
			.name("userFacingName")
			.build();
		when(projectRepository.findById(projectId.toString())).thenReturn(Optional.of(project));
		when(usersDAO.findById(id)).thenReturn(Optional.of(FURMSUser.builder()
			.id(id)
			.email("email")
			.build())
		);
		service.addUser(communityId.toString(), projectId.toString(), id);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.ROLE_ASSIGNMENT, argument.getValue().operationCategory);
		assertEquals(Action.GRANT, argument.getValue().action);
	}

	@Test
	void shouldDetectUserRemoval() {
		UUID communityId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		PersistentId id = new PersistentId("id");
		Project project = Project.builder()
			.name("userFacingName")
			.build();
		when(projectRepository.findById(projectId.toString())).thenReturn(Optional.of(project));
		when(usersDAO.findById(id)).thenReturn(Optional.of(FURMSUser.builder()
			.id(id)
			.email("email")
			.build())
		);
		service.removeUser(communityId.toString(), projectId.toString(), id);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.ROLE_ASSIGNMENT, argument.getValue().operationCategory);
		assertEquals(Action.REVOKE, argument.getValue().action);
	}
}
