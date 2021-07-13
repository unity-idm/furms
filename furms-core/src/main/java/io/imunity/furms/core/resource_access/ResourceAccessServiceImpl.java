/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.core.utils.AfterCommitLauncher.runAfterCommit;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_WRITE;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_READ;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_FAILED;
import static io.imunity.furms.domain.resource_access.AccessStatus.REVOKE_PENDING;

@Service
class ResourceAccessServiceImpl implements ResourceAccessService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteAgentResourceAccessService siteAgentResourceAccessService;
	private final ResourceAccessRepository repository;
	private final UserOperationRepository userRepository;
	private final UserOperationService userOperationService;
	private final AuthzService authzService;

	ResourceAccessServiceImpl(SiteAgentResourceAccessService siteAgentResourceAccessService,
	                          ResourceAccessRepository repository, UserOperationService userOperationService,
	                          UserOperationRepository userRepository, AuthzService authzService) {
		this.siteAgentResourceAccessService = siteAgentResourceAccessService;
		this.repository = repository;
		this.userRepository = userRepository;
		this.userOperationService = userOperationService;
		this.authzService = authzService;
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
		if(isUserProvided(userAdditionStatus)){
			repository.create(correlationId, grantAccess, AccessStatus.GRANT_PENDING);
			runAfterCommit(() ->
				siteAgentResourceAccessService.grantAccess(correlationId, grantAccess)
			);
		}
		else {
			repository.create(correlationId, grantAccess, AccessStatus.USER_INSTALLING);
			if(isUserNotProviding(userAdditionStatus))
				userOperationService.createUserAdditions(grantAccess.siteId, grantAccess.projectId, grantAccess.fenixUserId);
		}

		LOG.info("UserAllocation with correlation id {} was created {}", correlationId.id, grantAccess);
	}

	private boolean isUserProvided(Optional<UserStatus> userAdditionStatus) {
		return !(userAdditionStatus.isEmpty() || userAdditionStatus.get().equals(UserStatus.ADDING_PENDING) || userAdditionStatus.get().equals(UserStatus.ADDING_ACKNOWLEDGED));
	}

	private boolean isUserNotProviding(Optional<UserStatus> userAdditionStatus) {
		return userAdditionStatus.isEmpty() || userAdditionStatus.get().equals(UserStatus.ADDING_FAILED);
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
