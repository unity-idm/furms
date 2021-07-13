/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.users.UserAllocationsService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.sites.UserProjectsInstallationInfoData;
import io.imunity.furms.domain.sites.UserSitesInstallationInfoData;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.core.utils.AfterCommitLauncher.runAfterCommit;
import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.user_operation.UserStatus.ADDING_FAILED;
import static io.imunity.furms.domain.user_operation.UserStatus.ADDING_PENDING;
import static io.imunity.furms.domain.user_operation.UserStatus.REMOVAL_PENDING;
import static java.util.stream.Collectors.toSet;

@Service
public class UserOperationService implements UserAllocationsService {
	private final AuthzService authzService;
	private final SiteAgentUserService siteAgentUserService;
	private final SiteService siteService;
	private final UserOperationRepository repository;
	private final UsersDAO usersDAO;

	UserOperationService(AuthzService authzService,
	                     SiteService siteService, UserOperationRepository repository,
	                     SiteAgentUserService siteAgentUserService,
	                     UsersDAO usersDAO) {
		this.authzService = authzService;
		this.siteService = siteService;
		this.repository = repository;
		this.siteAgentUserService = siteAgentUserService;
		this.usersDAO = usersDAO;
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public Set<UserSitesInstallationInfoData> findCurrentUserSitesInstallations() {
		final PersistentId currentUserId = authzService.getCurrentUserId();
		final String fenixUserId = Optional.ofNullable(usersDAO.getFenixUserId(currentUserId))
				.map(fenixUser -> fenixUser.id)
				.orElse(null);

		return siteService.findUserSites(currentUserId).stream()
				.map(site -> UserSitesInstallationInfoData.builder()
						.siteName(site.getName())
						.connectionInfo(site.getConnectionInfo())
						.projects(loadProjects(fenixUserId, site.getId()))
						.build())
				.collect(toSet());
	}

	private Set<UserProjectsInstallationInfoData> loadProjects(String fenixUserId, String siteId) {
		return repository.findAllUserAdditionsWithSiteAndProjectBySiteId(fenixUserId, siteId).stream()
			.map(userAddition -> UserProjectsInstallationInfoData.builder()
				.name(userAddition.getProjectName())
				.remoteAccountName(userAddition.getUserId())
				.status(userAddition.getStatus())
				.errorMessage(userAddition.getErrorMessage())
				.build()
			).collect(toSet());
	}

	public void createUserAdditions(SiteId siteId, String projectId, FenixUserId userId) {
		Optional<FURMSUser> user = usersDAO.findById(userId);
		if(user.isEmpty())
			throw new IllegalArgumentException(String.format("User %s doesn't exist", userId));

		if(repository.existsByUserIdAndProjectId(userId, projectId))
			throw new IllegalArgumentException(String.format("User %s is already added to project %s", user, projectId));

		UserAddition userAddition = UserAddition.builder()
			.correlationId(CorrelationId.randomID())
			.projectId(projectId)
			.siteId(siteId)
			.userId(userId.id)
			.status(ADDING_PENDING)
			.build();
		repository.create(userAddition);
		runAfterCommit(() ->
			siteAgentUserService.addUser(userAddition, user.get())
		);
	}

	public void createUserRemovals(String projectId, PersistentId userId) {
		FURMSUser user = usersDAO.findById(userId).get();
		createUserRemovals(projectId, user);
	}

	public void createUserRemovals(String projectId, FenixUserId userId) {
		FURMSUser user = usersDAO.findById(userId).get();
		createUserRemovals(projectId, user);
	}

	private void createUserRemovals(String projectId, FURMSUser user) {
		String fenixUserId = user.fenixUserId.map(uId -> uId.id).orElse(null);
		repository.findAllUserAdditions(projectId, fenixUserId).stream()
			.filter(userAddition -> userAddition.status.isTransitionalTo(REMOVAL_PENDING))
			.forEach(userAddition -> {
				if(userAddition.status.equals(ADDING_FAILED)) {
					repository.delete(userAddition);
					return;
				}
				UserAddition addition = UserAddition.builder()
					.id(userAddition.id)
					.userId(userAddition.userId)
					.projectId(userAddition.projectId)
					.siteId(userAddition.siteId)
					.uid(userAddition.uid)
					.correlationId(CorrelationId.randomID())
					.status(REMOVAL_PENDING)
					.build();
				repository.update(addition);
				runAfterCommit(() ->
					siteAgentUserService.removeUser(addition)
				);
			});
	}

}
