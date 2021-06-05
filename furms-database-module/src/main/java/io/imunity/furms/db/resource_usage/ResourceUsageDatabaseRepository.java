/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_usage;

import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ResourceUsageDatabaseRepository implements ResourceUsageRepository {
	private final ResourceUsageEntityRepository resourceUsageEntityRepository;
	private final UserResourceUsageEntityRepository userResourceUsageEntityRepository;

	ResourceUsageDatabaseRepository(ResourceUsageEntityRepository resourceUsageEntityRepository, UserResourceUsageEntityRepository userResourceUsageEntityRepository) {
		this.resourceUsageEntityRepository = resourceUsageEntityRepository;
		this.userResourceUsageEntityRepository = userResourceUsageEntityRepository;
	}

	@Override
	public void create(ResourceUsage resourceUsage) {
		resourceUsageEntityRepository.save(
			ResourceUsageEntity.builder()
				.projectId(UUID.fromString(resourceUsage.projectId))
				.projectAllocationId(UUID.fromString(resourceUsage.projectAllocationId))
				.cumulativeConsumption(resourceUsage.cumulativeConsumption)
				.probedAt(resourceUsage.utcProbedAt)
				.build()
		);
	}

	@Override
	public void create(UserResourceUsage userResourceUsage) {
		userResourceUsageEntityRepository.save(
			UserResourceUsageEntity.builder()
				.projectId(UUID.fromString(userResourceUsage.projectId))
				.projectAllocationId(UUID.fromString(userResourceUsage.projectAllocationId))
				.fenixUserId(userResourceUsage.fenixUserId.id)
				.cumulativeConsumption(userResourceUsage.cumulativeConsumption)
				.consumedUntil(userResourceUsage.utcConsumedUntil)
				.build()
		);
	}

	@Override
	public Set<ResourceUsage> findNewestResourceUsages(String projectId) {
		return resourceUsageEntityRepository.findAllNewestByProjectId(UUID.fromString(projectId)).stream()
			.map(ResourceUsageEntity::toResourceUsage)
			.collect(Collectors.toSet());
	}

	@Override
	public Optional<ResourceUsage> findNewestResourceUsage(String projectAllocationId) {
		return resourceUsageEntityRepository.findNewestByProjectAllocationId(UUID.fromString(projectAllocationId))
			.map(ResourceUsageEntity::toResourceUsage);
	}
}
