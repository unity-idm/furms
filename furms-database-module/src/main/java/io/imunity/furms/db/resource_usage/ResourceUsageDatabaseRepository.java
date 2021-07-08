/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_usage;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.ResourceUsageByCommunityAllocation;
import io.imunity.furms.domain.resource_usage.ResourceUsageByCredit;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toSet;

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
	public void create(ResourceUsage resourceUsage, ProjectAllocationResolved projectAllocationResolved) {
		resourceUsageHistoryEntityRepository.save(
			ResourceUsageHistoryEntity.builder()
				.siteId(UUID.fromString(projectAllocationResolved.site.getId()))
				.communityId(UUID.fromString(projectAllocationResolved.communityAllocation.communityId))
				.communityAllocationId(UUID.fromString(projectAllocationResolved.communityAllocation.id))
				.resourceCreditId(UUID.fromString(projectAllocationResolved.resourceCredit.id))
				.projectId(UUID.fromString(resourceUsage.projectId))
				.projectAllocationId(UUID.fromString(resourceUsage.projectAllocationId))
				.cumulativeConsumption(resourceUsage.cumulativeConsumption)
				.probedAt(resourceUsage.utcProbedAt)
				.build()
		);
		Optional<ResourceUsageEntity> resourceUsageEntity = resourceUsageEntityRepository.findByProjectAllocationId(UUID.fromString(resourceUsage.projectAllocationId));
		if(resourceUsageEntity.isPresent() && resourceUsageEntity.get().cumulativeConsumption.compareTo(resourceUsage.cumulativeConsumption) > 0)
			LOG.warn("Update resource usage {} to {} from {}", resourceUsageEntity.get().id, resourceUsage.cumulativeConsumption, resourceUsageEntity.get().cumulativeConsumption);

		long id = resourceUsageEntity
			.map(usageEntity -> usageEntity.id)
			.orElse(0L);
		resourceUsageEntityRepository.save(
			ResourceUsageEntity.builder()
				.id(id)
				.siteId(UUID.fromString(projectAllocationResolved.site.getId()))
				.communityId(UUID.fromString(projectAllocationResolved.communityAllocation.communityId))
				.communityAllocationId(UUID.fromString(projectAllocationResolved.communityAllocation.id))
				.resourceCreditId(UUID.fromString(projectAllocationResolved.resourceCredit.id))
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
			LOG.warn("Update user resource usage {} to {} from {}", userResourceUsageEntity.get().id, userResourceUsage.cumulativeConsumption, userResourceUsageEntity.get().cumulativeConsumption);

		long id = userResourceUsageEntity
			.map(usage -> usage.id)
			.orElse(0L);
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
	public ResourceUsageByCredit findResourceUsagesSumsBySiteId(String siteId) {
		Map<String, BigDecimal> map = resourceUsageEntityRepository.findAllBySiteId(UUID.fromString(siteId)).stream()
			.collect(getResourceUsageSumCollector(resourceUsage -> resourceUsage.resourceCreditId.toString()));
		return new ResourceUsageByCredit(map);
	}

	@Override
	public ResourceUsageByCommunityAllocation findResourceUsagesSumsByCommunityId(String communityId) {
		Map<String, BigDecimal> map = resourceUsageEntityRepository.findAllByCommunityId(UUID.fromString(communityId)).stream()
			.collect(getResourceUsageSumCollector(resourceUsage -> resourceUsage.communityAllocationId.toString()));
		return new ResourceUsageByCommunityAllocation(map);
	}

	@Override
	public Set<UserResourceUsage> findUserResourceUsages(Set<UUID> projectAllocations, LocalDateTime from, LocalDateTime to) {
		return userResourceUsageHistoryEntityRepository.findAllByProjectAllocationIdIn(projectAllocations)
				.stream().map(UserResourceUsageHistoryEntity::toUserResourceUsage)
				.collect(toSet());
	}

	private Collector<ResourceUsageEntity, ?, Map<String, BigDecimal>> getResourceUsageSumCollector(Function<ResourceUsageEntity, String> classifier) {
		return groupingBy(
					classifier,
					reducing(
						BigDecimal.ZERO,
						resourceUsage -> resourceUsage.cumulativeConsumption,
						BigDecimal::add
					)
			);
	}

	@Override
	public Set<ResourceUsage> findCurrentResourceUsages(String projectId) {
		return resourceUsageEntityRepository.findAllByProjectId(UUID.fromString(projectId)).stream()
			.map(ResourceUsageEntity::toResourceUsage)
			.collect(toSet());
	}

	@Override
	public Optional<ResourceUsage> findCurrentResourceUsage(String projectAllocationId) {
		return resourceUsageEntityRepository.findByProjectAllocationId(UUID.fromString(projectAllocationId))
			.map(ResourceUsageEntity::toResourceUsage);
	}
}
