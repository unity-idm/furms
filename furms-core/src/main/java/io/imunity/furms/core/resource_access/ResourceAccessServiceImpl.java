/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.notification.PolicyNotificationService;
import io.imunity.furms.core.user_site_access.UserSiteAccessInnerService;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.resource_access.UserGrantAddedEvent;
import io.imunity.furms.domain.resource_access.UsersWithProjectAccess;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.core.utils.AfterCommitLauncher.runAfterCommit;
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

	ResourceAccessServiceImpl(SiteAgentResourceAccessService siteAgentResourceAccessService,
	                          ResourceAccessRepository repository,
	                          UserOperationRepository userRepository,
	                          AuthzService authzService,
	                          UserSiteAccessInnerService userSiteAccessInnerService,
	                          PolicyNotificationService policyNotificationService,
	                          ApplicationEventPublisher publisher) {
		this.siteAgentResourceAccessService = siteAgentResourceAccessService;
		this.repository = repository;
		this.userRepository = userRepository;
		this.authzService = authzService;
		this.policyNotificationService = policyNotificationService;
		this.userSiteAccessInnerService = userSiteAccessInnerService;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<String> findAddedUser(String projectId) {
		return userRepository.findUserIds(projectId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<UsersWithProjectAccess> findAddedUserBySiteId(String siteId) {
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
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<UserGrant> findUsersGrants(String projectId) {
		return repository.findUsersGrantsByProjectId(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public Set<UserGrant> findCurrentUserGrants(String projectId) {
		return repository.findUserGrantsByProjectIdAndFenixUserId(projectId, authzService.getCurrentAuthNUser().fenixUserId.orElseThrow(UserWithoutFenixIdValidationError::new));
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "grantAccess.projectId")
	public void grantAccess(GrantAccess grantAccess) {
		CorrelationId correlationId = CorrelationId.randomID();
		if(repository.exists(grantAccess))
			throw new IllegalArgumentException("Trying to create GrantAccess, which already exists: " + grantAccess);

		Optional<UserStatus> userAdditionStatus = userRepository.findAdditionStatus(grantAccess.siteId.id, grantAccess.projectId, grantAccess.fenixUserId);
		UUID grantId = createGrant(grantAccess, correlationId, userAdditionStatus);
		policyNotificationService.notifyAboutAllNotAcceptedPolicies(grantAccess.siteId.id, grantAccess.fenixUserId, grantId.toString());
		LOG.info("UserAllocation with correlation id {} was created {}", correlationId.id, grantAccess);
	}

	private UUID createGrant(GrantAccess grantAccess, CorrelationId correlationId, Optional<UserStatus> userAdditionStatus) {
		UUID grantId;
		if(isUserProvisioned(userAdditionStatus)) {
			grantId = repository.create(correlationId, grantAccess, AccessStatus.GRANT_PENDING);
			runAfterCommit(() ->
				siteAgentResourceAccessService.grantAccess(correlationId, grantAccess)
			);
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
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "grantAccess.projectId")
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
		runAfterCommit(() ->
			siteAgentResourceAccessService.revokeAccess(correlationId, grantAccess)
		);
		LOG.info("UserAllocation status with correlation id {} was changed to {}", correlationId.id, REVOKE_PENDING);
	}
}
