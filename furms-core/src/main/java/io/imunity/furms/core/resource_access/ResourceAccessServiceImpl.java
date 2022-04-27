/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.policy_documents.PolicyNotificationService;
import io.imunity.furms.core.post_commit.PostCommitRunner;
import io.imunity.furms.core.user_site_access.UserSiteAccessInnerService;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.GrantId;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.resource_access.UserGrantAddedEvent;
import io.imunity.furms.domain.resource_access.UserGrantRemovedCommissionEvent;
import io.imunity.furms.domain.resource_access.UsersWithProjectAccess;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_WRITE;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_FAILED;
import static io.imunity.furms.domain.resource_access.AccessStatus.REVOKE_PENDING;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
class ResourceAccessServiceImpl implements ResourceAccessService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteAgentResourceAccessService siteAgentResourceAccessService;
	private final ResourceAccessRepository repository;
	private final UserOperationRepository userRepository;
	private final AuthzService authzService;
	private final PolicyNotificationService policyNotificationService;
	private final UserSiteAccessInnerService userSiteAccessInnerService;
	private final ApplicationEventPublisher publisher;
	private final UsersDAO usersDAO;
	private final PostCommitRunner postCommitRunner;

	ResourceAccessServiceImpl(SiteAgentResourceAccessService siteAgentResourceAccessService,
	                          ResourceAccessRepository repository,
	                          UserOperationRepository userRepository,
	                          AuthzService authzService,
	                          UserSiteAccessInnerService userSiteAccessInnerService,
	                          PolicyNotificationService policyNotificationService,
	                          ApplicationEventPublisher publisher,
	                          UsersDAO usersDAO,
	                          PostCommitRunner postCommitRunner) {
		this.siteAgentResourceAccessService = siteAgentResourceAccessService;
		this.repository = repository;
		this.userRepository = userRepository;
		this.authzService = authzService;
		this.policyNotificationService = policyNotificationService;
		this.userSiteAccessInnerService = userSiteAccessInnerService;
		this.publisher = publisher;
		this.usersDAO = usersDAO;
		this.postCommitRunner = postCommitRunner;
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId.id")
	public Set<String> findAddedUser(ProjectId projectId) {
		return userRepository.findUserIds(projectId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId.id")
	public Set<UsersWithProjectAccess> findAddedUserBySiteId(SiteId siteId) {
		return userRepository.findAllUserAdditionsBySiteId(siteId).stream()
				.collect(groupingBy(userAddition -> userAddition.projectId))
				.entrySet().stream()
				.map(entry -> UsersWithProjectAccess.builder()
						.projectId(entry.getKey())
						.userIds(entry.getValue().stream()
								.map(userAddition -> userAddition.userId)
								.collect(toList()))
						.build())
				.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId.id")
	public Set<UserGrant> findUsersGrants(ProjectId projectId) {
		return repository.findUsersGrantsByProjectId(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId.id")
	public Set<UserGrant> findCurrentUserGrants(ProjectId projectId) {
		return repository.findUserGrantsByProjectIdAndFenixUserId(projectId, authzService.getCurrentAuthNUser().fenixUserId.orElseThrow(UserWithoutFenixIdValidationError::new));
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "grantAccess.projectId.id")
	public void grantAccess(GrantAccess grantAccess) {
		CorrelationId correlationId = CorrelationId.randomID();
		if(repository.exists(grantAccess))
			throw new IllegalArgumentException("Trying to create GrantAccess, which already exists: " + grantAccess);

		Optional<UserStatus> userAdditionStatus = userRepository.findAdditionStatus(grantAccess.siteId, grantAccess.projectId, grantAccess.fenixUserId);
		GrantId grantId = createGrant(grantAccess, correlationId, userAdditionStatus);
		policyNotificationService.notifyAboutAllNotAcceptedPolicies(grantAccess.siteId, grantAccess.fenixUserId, grantId);
		LOG.info("UserAllocation with correlation id {} was created {}", correlationId.id, grantAccess);
	}

	private GrantId createGrant(GrantAccess grantAccess, CorrelationId correlationId, Optional<UserStatus> userAdditionStatus) {
		GrantId grantId;
		if(isUserProvisioned(userAdditionStatus)) {
			grantId = repository.create(correlationId, grantAccess, AccessStatus.GRANT_PENDING);
			FURMSUser furmsUser = usersDAO.findById(grantAccess.fenixUserId).get();
			postCommitRunner.runAfterCommit(() -> siteAgentResourceAccessService.grantAccess(correlationId,
				grantAccess, furmsUser));
		}
		else {
			userSiteAccessInnerService.addAccessToSite(grantAccess);
			grantId = repository.create(correlationId, grantAccess, AccessStatus.USER_INSTALLING);
		}

		publisher.publishEvent(new UserGrantAddedEvent(grantAccess));
		return grantId;
	}

	private boolean isUserProvisioned(Optional<UserStatus> userAdditionStatus) {
		return userAdditionStatus.isPresent() && userAdditionStatus.get().isInstalled();
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "grantAccess.projectId.id")
	public void revokeAccess(GrantAccess grantAccess) {
		AccessStatus currentStatus = repository.findCurrentStatus(grantAccess.fenixUserId, grantAccess.allocationId);
		if(currentStatus.equals(GRANT_FAILED)) {
			repository.deleteByUserAndAllocationId(grantAccess.fenixUserId, grantAccess.allocationId);
			LOG.info("UserAllocation with user id {} and project allocation id {} was removed", grantAccess.fenixUserId, grantAccess.allocationId);
			return;
		}
		if(!currentStatus.isTransitionalTo(REVOKE_PENDING))
			throw new IllegalArgumentException(String.format("Transition between %s and %s states is not allowed", currentStatus, REVOKE_PENDING));
		CorrelationId correlationId = CorrelationId.randomID();
		repository.update(correlationId, grantAccess, REVOKE_PENDING);
		postCommitRunner.runAfterCommit(() -> {
				siteAgentResourceAccessService.revokeAccess(correlationId, grantAccess);
				publisher.publishEvent(new UserGrantRemovedCommissionEvent(grantAccess));

		});
		LOG.info("UserAllocation status with correlation id {} was changed to {}", correlationId.id, REVOKE_PENDING);
	}
}
