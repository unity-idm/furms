/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Set;

import static io.imunity.furms.core.utils.AfterCommitLauncher.runAfterCommit;
import static io.imunity.furms.domain.authz.roles.Capability.*;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_FAILED;
import static io.imunity.furms.domain.resource_access.AccessStatus.REVOKE_PENDING;

@Service
class ResourceAccessServiceImpl implements ResourceAccessService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteAgentResourceAccessService siteAgentResourceAccessService;
	private final ResourceAccessRepository repository;
	private final UserOperationRepository userRepository;

	ResourceAccessServiceImpl(SiteAgentResourceAccessService siteAgentResourceAccessService,
	                          ResourceAccessRepository repository,
	                          UserOperationRepository userRepository) {
		this.siteAgentResourceAccessService = siteAgentResourceAccessService;
		this.repository = repository;
		this.userRepository = userRepository;
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<UserGrant> findUsersGrants(String projectId) {
		return repository.findUsersGrants(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public Set<UserGrant> findUsersGrants(String projectId, FenixUserId fenixUserId) {
		return repository.findUsersGrants(projectId, fenixUserId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<String> findAddedUser(String projectId) {
		return userRepository.findUserIds(projectId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "grantAccess.projectId")
	public void grantAccess(GrantAccess grantAccess) {
		CorrelationId correlationId = CorrelationId.randomID();
		if(repository.exists(grantAccess))
			throw new IllegalArgumentException("Trying to create GrantAccess, which already exists: " + grantAccess);
		repository.create(correlationId, grantAccess);
		runAfterCommit(() ->
			siteAgentResourceAccessService.grantAccess(correlationId, grantAccess)
		);
		LOG.info("UserAllocation with correlation id {} was created {}", correlationId.id, grantAccess);
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
