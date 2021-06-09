/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_usage;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ResourceUsageDatabaseRepository implements ResourceUsageRepository {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


	private final ResourceUsageEntityRepository resourceUsageEntityRepository;
	private final ResourceUsageHistoryEntityRepository resourceUsageHistoryEntityRepository;
	private final UserResourceUsageEntityRepository userResourceUsageEntityRepository;
	private final UserResourceUsageHistoryEntityRepository userResourceUsageHistoryEntityRepository;

	ResourceUsageDatabaseRepository(ResourceUsageEntityRepository resourceUsageEntityRepository,
	                                ResourceUsageHistoryEntityRepository resourceUsageHistoryEntityRepository,
	                                UserResourceUsageEntityRepository userResourceUsageEntityRepository,
	                                UserResourceUsageHistoryEntityRepository userResourceUsageHistoryEntityRepository) {
		this.resourceUsageEntityRepository = resourceUsageEntityRepository;
		this.resourceUsageHistoryEntityRepository = resourceUsageHistoryEntityRepository;
		this.userResourceUsageEntityRepository = userResourceUsageEntityRepository;
		this.userResourceUsageHistoryEntityRepository = userResourceUsageHistoryEntityRepository;
	}

	@Override
	public void create(ResourceUsage resourceUsage) {
		resourceUsageHistoryEntityRepository.save(
			ResourceUsageHistoryEntity.builder()
				.projectId(UUID.fromString(resourceUsage.projectId))
				.projectAllocationId(UUID.fromString(resourceUsage.projectAllocationId))
				.cumulativeConsumption(resourceUsage.cumulativeConsumption)
				.probedAt(resourceUsage.utcProbedAt)
				.build()
		);
		Optional<ResourceUsageEntity> resourceUsageEntity = resourceUsageEntityRepository.findByProjectAllocationId(UUID.fromString(resourceUsage.projectAllocationId));
		if(resourceUsageEntity.isPresent() && resourceUsageEntity.get().cumulativeConsumption.compareTo(resourceUsage.cumulativeConsumption) > 0)
			LOG.warn("Update resource usage {} to {} from {}", resourceUsageEntity.get().getId(), resourceUsage.cumulativeConsumption, resourceUsageEntity.get().cumulativeConsumption);

		UUID id = resourceUsageEntity
			.map(UUIDIdentifiable::getId)
			.orElse(null);
		resourceUsageEntityRepository.save(
			ResourceUsageEntity.builder()
				.id(id)
				.projectId(UUID.fromString(resourceUsage.projectId))
				.projectAllocationId(UUID.fromString(resourceUsage.projectAllocationId))
				.cumulativeConsumption(resourceUsage.cumulativeConsumption)
				.probedAt(resourceUsage.utcProbedAt)
				.build()
		);
	}

	@Override
	public void create(UserResourceUsage userResourceUsage) {
		userResourceUsageHistoryEntityRepository.save(
			UserResourceUsageHistoryEntity.builder()
				.projectId(UUID.fromString(userResourceUsage.projectId))
				.projectAllocationId(UUID.fromString(userResourceUsage.projectAllocationId))
				.fenixUserId(userResourceUsage.fenixUserId.id)
				.cumulativeConsumption(userResourceUsage.cumulativeConsumption)
				.consumedUntil(userResourceUsage.utcConsumedUntil)
				.build()
		);
		Optional<UserResourceUsageEntity> userResourceUsageEntity = userResourceUsageEntityRepository.findByProjectAllocationId(UUID.fromString(userResourceUsage.projectAllocationId));
		if(userResourceUsageEntity.isPresent() && userResourceUsageEntity.get().cumulativeConsumption.compareTo(userResourceUsage.cumulativeConsumption) > 0)
			LOG.warn("Update user resource usage {} to {} from {}", userResourceUsageEntity.get().getId(), userResourceUsage.cumulativeConsumption, userResourceUsageEntity.get().cumulativeConsumption);

		UUID id = userResourceUsageEntity
			.map(UUIDIdentifiable::getId)
			.orElse(null);
		userResourceUsageEntityRepository.save(
			UserResourceUsageEntity.builder()
				.id(id)
				.projectId(UUID.fromString(userResourceUsage.projectId))
				.projectAllocationId(UUID.fromString(userResourceUsage.projectAllocationId))
				.fenixUserId(userResourceUsage.fenixUserId.id)
				.cumulativeConsumption(userResourceUsage.cumulativeConsumption)
				.consumedUntil(userResourceUsage.utcConsumedUntil)
				.build()
		);
	}

	@Override
	public Set<ResourceUsage> findCurrentResourceUsages(String projectId) {
		return resourceUsageEntityRepository.findAllByProjectId(UUID.fromString(projectId)).stream()
			.map(ResourceUsageEntity::toResourceUsage)
			.collect(Collectors.toSet());
	}

	@Override
	public Optional<ResourceUsage> findCurrentResourceUsage(String projectAllocationId) {
		return resourceUsageEntityRepository.findByProjectAllocationId(UUID.fromString(projectAllocationId))
			.map(ResourceUsageEntity::toResourceUsage);
	}
}
