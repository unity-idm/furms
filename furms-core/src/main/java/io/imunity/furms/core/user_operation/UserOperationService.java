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
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.sites.UserProjectsInstallationInfoData;
import io.imunity.furms.domain.sites.UserSitesInstallationInfoData;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.user_operation.UserStatus.*;
import static java.util.stream.Collectors.toSet;

@Service
public class UserOperationService implements UserAllocationsService {
	private final AuthzService authzService;
	private final SiteAgentUserService siteAgentUserService;
	private final SiteService siteService;

	private final UserOperationRepository repository;
	private final ProjectRepository projectRepository;
	private final SiteRepository siteRepository;

	private final ProjectGroupsDAO projectGroupsDAO;
	private final UsersDAO usersDAO;

	UserOperationService(AuthzService authzService,
	                     SiteService siteService, UserOperationRepository repository,
	                     SiteAgentUserService siteAgentUserService,
	                     SiteRepository siteRepository,
	                     ProjectGroupsDAO projectGroupsDAO,
	                     UsersDAO usersDAO,
	                     ProjectRepository projectRepository) {
		this.authzService = authzService;
		this.siteService = siteService;
		this.repository = repository;
		this.projectRepository = projectRepository;
		this.siteAgentUserService = siteAgentUserService;
		this.siteRepository = siteRepository;
		this.projectGroupsDAO = projectGroupsDAO;
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

	public void createUserAdditions(String projectId, PersistentId userId) {
		FURMSUser user = usersDAO.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("User with id %s doesn't exist", userId)));
		FenixUserId fenixUserId = user.fenixUserId
			.orElseThrow(() -> new IllegalArgumentException(String.format("FenixId for user with id %s is not present", userId)));
		if(repository.existsByUserIdAndProjectId(fenixUserId, projectId))
			throw new IllegalArgumentException(String.format("User %s is already added to project %s", user.fenixUserId, projectId));
		siteRepository.findByProjectId(projectId)
			.forEach(siteId -> {
				UserAddition userAddition = UserAddition.builder()
					.correlationId(CorrelationId.randomID())
					.projectId(projectId)
					.siteId(siteId)
					.userId(user.fenixUserId.map(uId -> uId.id).orElse(null))
					.status(ADDING_PENDING)
					.build();
				repository.create(userAddition);
				siteAgentUserService.addUser(userAddition, user);
			});
	}

	public void createUserAdditions(String siteId, String projectId) {
		Site site = siteRepository.findById(siteId).get();
		String communityId = projectRepository.findById(projectId).get().getCommunityId();
		projectGroupsDAO.getAllUsers(communityId, projectId)
			.forEach(user -> {
				UserAddition userAddition = UserAddition.builder()
					.correlationId(CorrelationId.randomID())
					.projectId(projectId)
					.siteId(new SiteId(site.getId(), site.getExternalId()))
					.userId(user.fenixUserId.map(userId -> userId.id).orElse(null))
					.status(ADDING_PENDING)
					.build();
				repository.create(userAddition);
				siteAgentUserService.addUser(userAddition, user);
		});
	}

	public void createUserRemovals(String projectId, PersistentId userId) {
		FURMSUser user = usersDAO.findById(userId).get();
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
				siteAgentUserService.removeUser(addition);
			});
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
}
