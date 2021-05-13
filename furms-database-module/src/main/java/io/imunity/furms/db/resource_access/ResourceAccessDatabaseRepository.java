/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;


import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
class ResourceAccessDatabaseRepository implements ResourceAccessRepository {

	private final UserAllocationRepository userAllocationRepository;
	private final UserAllocationJobRepository userAllocationJobRepository;

	ResourceAccessDatabaseRepository(UserAllocationRepository userAllocationRepository, UserAllocationJobRepository userAllocationJobRepository) {
		this.userAllocationRepository = userAllocationRepository;
		this.userAllocationJobRepository = userAllocationJobRepository;
	}

	@Override
	public Set<UserGrant> findUsersGrants(String projectId) {
		return userAllocationRepository.findAll(UUID.fromString(projectId)).stream()
			.map(userAllocationResolved -> UserGrant.builder()
				.projectAllocationId(userAllocationResolved.allocation.projectAllocationId.toString())
				.userId(userAllocationResolved.allocation.userId)
				.status(AccessStatus.valueOf(userAllocationResolved.job.status))
				.message(userAllocationResolved.job.message)
				.build())
			.collect(Collectors.toSet());
	}

	@Override
	public void create(CorrelationId correlationId, GrantAccess grantAccess) {
		Optional<UserAllocationResolved> userAllocation = userAllocationRepository.findByUserIdAndProjectAllocationId(grantAccess.fenixUserId, UUID.fromString(grantAccess.allocationId));
		if(userAllocation.isPresent()){
			update(correlationId, userAllocation.get(), AccessStatus.GRANT_PENDING);
			return;
		}
		UserAllocationEntity save = userAllocationRepository.save(
			UserAllocationEntity.builder()
				.siteId(UUID.fromString(grantAccess.siteId.id))
				.projectId(UUID.fromString(grantAccess.projectId))
				.projectAllocationId(UUID.fromString(grantAccess.allocationId))
				.userId(grantAccess.fenixUserId)
				.build()
		);
		userAllocationJobRepository.save(UserAllocationJobEntity.builder()
			.correlationId(UUID.fromString(correlationId.id))
			.userAllocationId(save.getId())
			.status(AccessStatus.GRANT_PENDING)
			.build()
		);
	}

	@Override
	public void update(CorrelationId correlationId, GrantAccess grantAccess) {
		UserAllocationResolved userAllocation = userAllocationRepository
			.findByUserIdAndProjectAllocationId(grantAccess.fenixUserId, UUID.fromString(grantAccess.allocationId))
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));

		update(correlationId, userAllocation, AccessStatus.REVOKE_PENDING);
	}

	private void update(CorrelationId correlationId, UserAllocationResolved userAllocation, AccessStatus status) {
		userAllocationJobRepository.save(UserAllocationJobEntity.builder()
			.id(userAllocation.job.getId())
			.correlationId(UUID.fromString(correlationId.id))
			.userAllocationId(userAllocation.job.userAllocationId)
			.status(status)
			.build()
		);
	}

	@Override
	public void update(CorrelationId correlationId, AccessStatus status, String msg) {
		userAllocationJobRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(old ->
					UserAllocationJobEntity.builder()
						.id(old.getId())
						.userAllocationId(old.userAllocationId)
						.correlationId(old.correlationId)
						.status(status)
						.message(msg)
						.build()
				)
			.map(userAllocationJobRepository::save)
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
	}

	@Override
	public void delete(CorrelationId correlationId){
		UserAllocationJobEntity userAllocationJobEntity = userAllocationJobRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
		userAllocationRepository.deleteById(userAllocationJobEntity.userAllocationId);
	}

	@Override
	public void deleteAll() {
		userAllocationRepository.deleteAll();
	}


}
