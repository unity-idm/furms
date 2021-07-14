/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
	private ProjectRepository projectRepository;
	@Mock
	private AuthzService authzService;
	@Mock
	private SiteService siteService;

	private UserOperationService service;
	private InOrder orderVerifier;

	@BeforeEach
	void setUp() {
		TransactionSynchronizationManager.initSynchronization();
	}

	@AfterEach
	void clear() {
		TransactionSynchronizationManager.clear();
	}

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new UserOperationService(authzService, siteService, repository, siteAgentUserService, usersDAO);
		orderVerifier = inOrder(repository, siteAgentUserService);
	}

	@Test
	void shouldCreateUserAddition() {
		SiteId siteId = new SiteId("siteId", new SiteExternalId("id"));
		String projectId = "projectId";
		PersistentId userId = new PersistentId("userId");
		FenixUserId fenixUserId = new FenixUserId("id");
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.fenixUserId(fenixUserId)
			.email("email")
			.build();
		//when
		when(usersDAO.findById(fenixUserId)).thenReturn(Optional.of(user));
		when(siteRepository.findByProjectId(projectId)).thenReturn(Set.of(siteId));
		service.createUserAdditions(siteId, projectId, fenixUserId);
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
			.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}

		//then
		orderVerifier.verify(repository).create(any(UserAddition.class));
		orderVerifier.verify(siteAgentUserService).addUser(any(UserAddition.class), eq(user));
	}

	@Test
	void shouldNotCreateUserAddition() {
		SiteId siteId = new SiteId("siteId", new SiteExternalId("id"));
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
		when(siteRepository.findByProjectId(projectId)).thenReturn(Set.of(siteId));

		//then
		assertThrows(IllegalArgumentException.class, () -> service.createUserAdditions(siteId, projectId, id));
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
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
			.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}

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