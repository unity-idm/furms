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

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
class ResourceAccessDatabaseRepository implements ResourceAccessRepository {

	private final UserGrantEntityRepository userGrantEntityRepository;
	private final UserGrantJobEntityRepository userGrantJobEntityRepository;

	ResourceAccessDatabaseRepository(UserGrantEntityRepository userGrantEntityRepository, UserGrantJobEntityRepository userGrantJobEntityRepository) {
		this.userGrantEntityRepository = userGrantEntityRepository;
		this.userGrantJobEntityRepository = userGrantJobEntityRepository;
	}

	@Override
	public Set<UserGrant> findUsersGrants(String projectId) {
		return userGrantEntityRepository.findAll(UUID.fromString(projectId)).stream()
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
		UserGrantEntity save = userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(UUID.fromString(grantAccess.siteId.id))
				.projectId(UUID.fromString(grantAccess.projectId))
				.projectAllocationId(UUID.fromString(grantAccess.allocationId))
				.userId(grantAccess.fenixUserId.id)
				.build()
		);
		userGrantJobEntityRepository.save(UserGrantJobEntity.builder()
			.correlationId(UUID.fromString(correlationId.id))
			.userAllocationId(save.getId())
			.status(AccessStatus.GRANT_PENDING)
			.build()
		);
	}

	public boolean exists(GrantAccess grantAccess) {
		return userGrantEntityRepository.findByUserIdAndProjectAllocationId(grantAccess.fenixUserId.id, UUID.fromString(grantAccess.allocationId))
			.isPresent();
	}

	@Override
	public void update(CorrelationId correlationId, GrantAccess grantAccess, AccessStatus status) {
		UserGrantResolved userAllocation = userGrantEntityRepository
			.findByUserIdAndProjectAllocationId(grantAccess.fenixUserId.id, UUID.fromString(grantAccess.allocationId))
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));

		userGrantJobEntityRepository.save(UserGrantJobEntity.builder()
			.id(userAllocation.job.getId())
			.correlationId(UUID.fromString(correlationId.id))
			.userAllocationId(userAllocation.job.userAllocationId)
			.status(status)
			.build()
		);
	}

	@Override
	public void update(CorrelationId correlationId, AccessStatus status, String msg) {
		userGrantJobEntityRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(old ->
					UserGrantJobEntity.builder()
						.id(old.getId())
						.userAllocationId(old.userAllocationId)
						.correlationId(old.correlationId)
						.status(status)
						.message(msg)
						.build()
				)
			.map(userGrantJobEntityRepository::save)
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
	}

	@Override
	public void delete(CorrelationId correlationId){
		UserGrantJobEntity userGrantJobEntity = userGrantJobEntityRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
		userGrantEntityRepository.deleteById(userGrantJobEntity.userAllocationId);
	}

	@Override
	public void deleteAll() {
		userGrantEntityRepository.deleteAll();
	}


}
