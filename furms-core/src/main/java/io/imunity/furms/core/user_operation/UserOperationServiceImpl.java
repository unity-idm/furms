/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionStatus;
import io.imunity.furms.domain.user_operation.UserRemoval;
import io.imunity.furms.domain.user_operation.UserRemovalStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;

@Service
class UserOperationServiceImpl implements UserOperationService {
	private final UserOperationRepository repository;
	private final SiteAgentUserService siteAgentUserService;
	private final SiteRepository siteRepository;
	private final UsersDAO usersDAO;

	UserOperationServiceImpl(UserOperationRepository repository, SiteAgentUserService siteAgentUserService, SiteRepository siteRepository, UsersDAO usersDAO) {
		this.repository = repository;
		this.siteAgentUserService = siteAgentUserService;
		this.siteRepository = siteRepository;
		this.usersDAO = usersDAO;
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = PROJECT, id = "projectId")
	public void createUserAdditions(String projectId, PersistentId userId) {
		FURMSUser user = usersDAO.findById(userId).get();
		siteRepository.findByProjectId(projectId)
			.forEach(siteId -> {
				UserAddition userAddition = UserAddition.builder()
					.correlationId(CorrelationId.randomID())
					.projectId(projectId)
					.siteId(siteId)
					.status(UserAdditionStatus.PENDING)
					.build();
				repository.create(userAddition);
				siteAgentUserService.addUser(userAddition, user);
			});
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = PROJECT, id = "projectId")
	public void createUserRemovals(String projectId, PersistentId userId) {
		repository.findAllUserAdditions(projectId, userId.id).stream()
			.map(userAddition -> UserRemoval.builder()
				.correlationId(CorrelationId.randomID())
				.siteId(userAddition.siteId)
				.projectId(userAddition.projectId)
				.userAdditionId(userAddition.id)
				.uid(userAddition.uid)
				.status(UserRemovalStatus.PENDING)
				.build())
			.forEach(userRemoval -> {
				repository.create(userRemoval);
				siteAgentUserService.removeUser(userRemoval);
			});
	}
}
