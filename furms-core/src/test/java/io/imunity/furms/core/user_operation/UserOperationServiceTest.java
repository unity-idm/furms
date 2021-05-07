/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserRemoval;
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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

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
	void shouldCreateUserRemoval() {
		String projectId = "projectId";
		PersistentId userId = new PersistentId("userId");
		UserAddition userAddition = UserAddition.builder().build();

		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
			.email("email")
			.fenixUserId(new FenixUserId("userId"))
			.build()));
		when(repository.findAllUserAdditions(projectId, userId.id)).thenReturn(Set.of(userAddition));
		service.createUserRemovals(projectId, userId);

		//then
		orderVerifier.verify(repository).create(any(UserRemoval.class));
		orderVerifier.verify(siteAgentUserService).removeUser(any(UserRemoval.class));
	}
}