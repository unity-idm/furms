/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import static io.imunity.furms.core.utils.AfterCommitLauncher.runAfterCommit;
import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.USERS_MAINTENANCE;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static io.imunity.furms.domain.user_operation.UserStatus.ADDING_FAILED;
import static io.imunity.furms.domain.user_operation.UserStatus.ADDING_PENDING;
import static io.imunity.furms.domain.user_operation.UserStatus.REMOVAL_PENDING;
import static java.util.Comparator.comparingInt;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.api.users.UserAllocationsService;
import io.imunity.furms.api.validation.exceptions.UserInstallationOnSiteIsNotTerminalException;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceAtSite;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.domain.projects.ProjectMembershipOnSite;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.sites.SiteUser;
import io.imunity.furms.domain.sites.UserProjectsInstallationInfoData;
import io.imunity.furms.domain.sites.UserSitesInstallationInfoData;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.user_site_access.UserSiteAccessRepository;
import io.imunity.furms.spi.users.UsersDAO;

@Service
public class UserOperationService implements UserAllocationsService {
	private final AuthzService authzService;
	private final SiteAgentUserService siteAgentUserService;
	private final SiteService siteService;
	private final UserOperationRepository repository;
	private final UsersDAO usersDAO;
	private final PolicyDocumentServiceHelper policyService;
	private final SSHKeyService sshKeyService;
	private final ResourceAccessRepository resourceAccessRepository;
	private final UserSiteAccessRepository userSiteAccessRepository;

	UserOperationService(AuthzService authzService,
	                     SiteService siteService,
	                     UserOperationRepository repository,
	                     SiteAgentUserService siteAgentUserService,
	                     UsersDAO usersDAO,
	                     PolicyDocumentServiceHelper policyService,
	                     SSHKeyService sshKeyService,
	                     ResourceAccessRepository resourceAccessRepository,
	                     UserSiteAccessRepository userSiteAccessRepository) {
		this.authzService = authzService;
		this.siteService = siteService;
		this.repository = repository;
		this.siteAgentUserService = siteAgentUserService;
		this.usersDAO = usersDAO;
		this.policyService = policyService;
		this.sshKeyService = sshKeyService;
		this.resourceAccessRepository = resourceAccessRepository;
		this.userSiteAccessRepository = userSiteAccessRepository;
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED)
	public Set<UserSitesInstallationInfoData> findCurrentUserSitesInstallations() {
		final PersistentId currentUserId = authzService.getCurrentUserId();
		return findByUserId(currentUserId);
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE)
	public Set<SiteUser> findUserSitesInstallations(PersistentId userId) {
		final FenixUserId fenixUserId = ofNullable(usersDAO.getFenixUserId(userId))
				.orElse(null);
		final Map<String, Optional<PolicyAcceptanceAtSite>> sitesPolicy =
				policyService.findSitePolicyAcceptancesByUserId(fenixUserId).stream()
						.collect(groupingBy(
								policyAcceptance -> policyAcceptance.siteId,
								maxBy(comparingInt(policyAcceptance -> policyAcceptance.policyDocumentRevision))));
		final Map<String, Set<PolicyAcceptanceAtSite>> servicePolicies =
				policyService.findServicesPolicyAcceptancesByUserId(fenixUserId).stream()
						.collect(groupingBy(policyAcceptance -> policyAcceptance.siteId, toSet()));
		return findByUserId(userId).stream()
				.map(site -> SiteUser.builder()
						.siteId(site.getSiteId())
						.siteOauthClientId(site.getOauthClientId())
						.projectSitesMemberships(site.getProjects().stream()
								.map(project -> ProjectMembershipOnSite.builder()
										.projectId(project.getProjectId())
										.localUserId(project.getRemoteAccountName())
										.build())
								.collect(toSet()))
						.sitePolicyAcceptance(sitesPolicy.getOrDefault(site.getSiteId(), empty()).orElse(null))
						.servicesPolicyAcceptance(servicePolicies.getOrDefault(site.getSiteId(), Set.of()))
						.sshKeys(sshKeyService.findSiteSSHKeysByUserIdAndSite(userId, site.getSiteId()))
						.build())
				.collect(toSet());
	}

	private Set<UserSitesInstallationInfoData> findByUserId(PersistentId userId) {
		final String fenixUserId = ofNullable(usersDAO.getFenixUserId(userId))
				.map(fenixUser -> fenixUser.id)
				.orElse(null);

		return siteService.findUserSites(userId).stream()
				.map(site -> {
					final Set<UserProjectsInstallationInfoData> projects = loadProjects(fenixUserId, site.getId());
					return UserSitesInstallationInfoData.builder()
							.siteId(site.getId())
							.siteName(site.getName())
							.oauthClientId(site.getOauthClientId())
							.connectionInfo(site.getConnectionInfo())
							.projects(projects)
							.build();
				})
				.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE)
	public Set<UserAddition> findAllByFenixUserId(FenixUserId fenixUserId) {
		return repository.findAllUserAdditions(fenixUserId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<UserAddition> findAllBySiteId(String siteId) {
		return repository.findAllUserAdditionsBySiteId(siteId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<UserAddition> findAllByProjectId(String projectId) {
		return repository.findAllUserAdditionsByProjectId(projectId);
	}

	private Set<UserProjectsInstallationInfoData> loadProjects(String fenixUserId, String siteId) {
		return repository.findAllUserAdditionsWithSiteAndProjectBySiteId(fenixUserId, siteId).stream()
			.map(userAddition -> UserProjectsInstallationInfoData.builder()
				.projectId(userAddition.getProjectId())
				.name(userAddition.getProjectName())
				.remoteAccountName(userAddition.getUserId())
				.status(userAddition.getStatus())
				.errorMessage(userAddition.getErrorMessage())
				.build()
			).collect(toSet());
	}

	public void createUserAdditions(SiteId siteId, String projectId, UserPolicyAcceptancesWithServicePolicies userPolicyAcceptances) {
		FenixUserId userId = userPolicyAcceptances.user.fenixUserId.get();
		if(repository.existsByUserIdAndSiteIdAndProjectId(userId, siteId.id, projectId))
			throw new IllegalArgumentException(String.format("User %s is already added to project %s", userId, projectId));

		UserAddition userAddition = UserAddition.builder()
			.correlationId(CorrelationId.randomID())
			.projectId(projectId)
			.siteId(siteId)
			.userId(userId.id)
			.status(ADDING_PENDING)
			.build();
		repository.create(userAddition);
		runAfterCommit(() ->
			siteAgentUserService.addUser(userAddition, userPolicyAcceptances)
		);
	}

	public void createUserRemovals(String projectId, PersistentId userId) {
		FURMSUser user = usersDAO.findById(userId).get();
		createUserRemovals(projectId, user);
	}

	public void createUserRemovals(String siteId, String projectId, FenixUserId userId) {
		FURMSUser user = usersDAO.findById(userId).get();
		createUserRemovals(siteId, projectId, user);
	}

	private void createUserRemovals(String projectId, FURMSUser user) {
		String fenixUserId = user.fenixUserId.map(uId -> uId.id).orElse(null);
		Set<UserAddition> allUserAdditions = repository.findAllUserAdditions(projectId, fenixUserId);
		if(allUserAdditions.stream().anyMatch(userAddition -> !userAddition.status.isTransitionalTo(REMOVAL_PENDING)))
			throw new UserInstallationOnSiteIsNotTerminalException();
		allUserAdditions.forEach(this::removeUser);

		FenixUserId userId = new FenixUserId(fenixUserId);
		resourceAccessRepository.deleteByUserAndProjectId(userId, projectId);
		userSiteAccessRepository.remove(projectId, userId);
	}

	private void createUserRemovals(String siteId, String projectId, FURMSUser user) {
		String fenixUserId = user.fenixUserId.map(uId -> uId.id).orElse(null);
		repository.findUserAddition(siteId, projectId, fenixUserId)
			.filter(userAddition -> userAddition.status.isTransitionalTo(REMOVAL_PENDING))
			.ifPresent(this::removeUser);
			resourceAccessRepository.deleteByUserAndSiteIdAndProjectId(new FenixUserId(fenixUserId), siteId, projectId);
	}

	private void removeUser(UserAddition userAddition) {
		if (userAddition.status.equals(ADDING_FAILED)) {
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
	}

}
