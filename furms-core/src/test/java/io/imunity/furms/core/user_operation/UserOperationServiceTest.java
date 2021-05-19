/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserOperationServiceTest {
	@Mock
	private UserOperationRepository repository;
	@Mock
	private SiteAgentUserService siteAgentUserService;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private ProjectGroupsDAO projectGroupsDAO;
	@Mock
	private ProjectRepository projectRepository;

	private UserOperationService service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new UserOperationService(repository, siteAgentUserService, siteRepository, projectGroupsDAO, usersDAO, projectRepository);
		orderVerifier = inOrder(repository, siteAgentUserService);
	}

	@Test
	void shouldCreateUserAddition() {
		String projectId = "projectId";
		PersistentId userId = new PersistentId("userId");
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.fenixUserId(new FenixUserId("id"))
			.email("email")
			.build();
		//when
		when(usersDAO.findById(userId)).thenReturn(Optional.of(user));
		when(siteRepository.findByProjectId(projectId)).thenReturn(Set.of(new SiteId("siteId", new SiteExternalId("id"))));
		service.createUserAdditions(projectId, userId);

		//then
		orderVerifier.verify(repository).create(any(UserAddition.class));
		orderVerifier.verify(siteAgentUserService).addUser(any(UserAddition.class), eq(user));
	}

	@Test
	void shouldCreateAllUserAddition() {
		String siteId = "siteId";
		String projectId = "projectId";
		String communityId = "projectId";
		PersistentId userId = new PersistentId("userId");
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.email("email")
			.build();
		//when
		when(siteRepository.findById(siteId)).thenReturn(Optional.of(Site.builder()
			.id(siteId)
			.build()));
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(Project.builder()
			.id(projectId)
			.communityId(communityId)
			.build()));
		when(projectGroupsDAO.getAllUsers(communityId, projectId)).thenReturn(List.of(user));
		service.createUserAdditions(siteId, projectId);

		//then
		orderVerifier.verify(repository).create(any(UserAddition.class));
		orderVerifier.verify(siteAgentUserService).addUser(any(UserAddition.class), eq(user));
	}

	@Test
	void shouldNotCreateUserAddition() {
		String projectId = "projectId";
		PersistentId userId = new PersistentId("userId");
		FenixUserId id = new FenixUserId("id");
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.fenixUserId(id)
			.email("email")
			.build();
		//when
		when(usersDAO.findById(userId)).thenReturn(Optional.of(user));
		when(repository.existsByUserIdAndProjectId(id, projectId)).thenReturn(true);
		when(siteRepository.findByProjectId(projectId)).thenReturn(Set.of(new SiteId("siteId", new SiteExternalId("id"))));

		//then
		assertThrows(IllegalArgumentException.class, () -> service.createUserAdditions(projectId, userId));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"REMOVAL_FAILED", "ADDED"})
	void shouldCreateUserRemoval(UserStatus status) {
		String projectId = "projectId";
		PersistentId userId = new PersistentId("userId");
		UserAddition userAddition = UserAddition.builder()
			.status(status)
			.build();

		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
			.email("email")
			.fenixUserId(new FenixUserId("userId"))
			.build()));
		when(repository.findAllUserAdditions(projectId, userId.id)).thenReturn(Set.of(userAddition));
		service.createUserRemovals(projectId, userId);

		//then
		orderVerifier.verify(repository).update(any(UserAddition.class));
		orderVerifier.verify(siteAgentUserService).removeUser(any(UserAddition.class));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"REMOVAL_FAILED", "ADDED"}, mode = EXCLUDE)
	void shouldNotCreateUserRemoval(UserStatus status) {
		String projectId = "projectId";
		PersistentId userId = new PersistentId("userId");
		UserAddition userAddition = UserAddition.builder()
			.status(status)
			.build();

		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
			.email("email")
			.fenixUserId(new FenixUserId("userId"))
			.build()));
		when(repository.findAllUserAdditions(projectId, userId.id)).thenReturn(Set.of(userAddition));
		service.createUserRemovals(projectId, userId);

		//then
		verify(repository, times(0)).update(any(UserAddition.class));
		verify(siteAgentUserService, times(0)).removeUser(any(UserAddition.class));
	}
}