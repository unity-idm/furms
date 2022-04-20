/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_usage;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.ResourceUsageByCommunityAllocation;
import io.imunity.furms.domain.resource_usage.ResourceUsageByCredit;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.sites.SiteId;
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
import java.util.stream.Collectors;

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
				.communityId(projectAllocationResolved.communityAllocation.communityId.id)
				.communityAllocationId(projectAllocationResolved.communityAllocation.id.id)
				.resourceCreditId(projectAllocationResolved.resourceCredit.id.id)
				.projectId(resourceUsage.projectId.id)
				.projectAllocationId(resourceUsage.projectAllocationId.id)
				.cumulativeConsumption(resourceUsage.cumulativeConsumption)
				.probedAt(resourceUsage.utcProbedAt)
				.build()
		);
		Optional<ResourceUsageEntity> resourceUsageEntity =
			resourceUsageEntityRepository.findByProjectAllocationId(resourceUsage.projectAllocationId.id);
		if(resourceUsageEntity.isPresent() && resourceUsageEntity.get().cumulativeConsumption.compareTo(resourceUsage.cumulativeConsumption) > 0)
			LOG.warn("Update resource usage {} to {} from {}", resourceUsageEntity.get().id, resourceUsage.cumulativeConsumption, resourceUsageEntity.get().cumulativeConsumption);

		long id = resourceUsageEntity
			.map(usageEntity -> usageEntity.id)
			.orElse(0L);
		resourceUsageEntityRepository.save(
			ResourceUsageEntity.builder()
				.id(id)
				.siteId(UUID.fromString(projectAllocationResolved.site.getId()))
				.communityId(projectAllocationResolved.communityAllocation.communityId.id)
				.communityAllocationId(projectAllocationResolved.communityAllocation.id.id)
				.resourceCreditId(projectAllocationResolved.resourceCredit.id.id)
				.projectId(resourceUsage.projectId.id)
				.projectAllocationId(resourceUsage.projectAllocationId.id)
				.cumulativeConsumption(resourceUsage.cumulativeConsumption)
				.probedAt(resourceUsage.utcProbedAt)
				.build()
		);
	}

	@Override
	public void create(UserResourceUsage userResourceUsage) {
		userResourceUsageHistoryEntityRepository.save(
			UserResourceUsageHistoryEntity.builder()
				.projectId(userResourceUsage.projectId.id)
				.projectAllocationId(userResourceUsage.projectAllocationId.id)
				.fenixUserId(userResourceUsage.fenixUserId.id)
				.cumulativeConsumption(userResourceUsage.cumulativeConsumption)
				.consumedUntil(userResourceUsage.utcConsumedUntil)
				.build()
		);
		Optional<UserResourceUsageEntity> userResourceUsageEntity =
			userResourceUsageEntityRepository.findByProjectAllocationId(userResourceUsage.projectAllocationId.id);
		if(userResourceUsageEntity.isPresent() && userResourceUsageEntity.get().cumulativeConsumption.compareTo(userResourceUsage.cumulativeConsumption) > 0)
			LOG.warn("Update user resource usage {} to {} from {}", userResourceUsageEntity.get().id, userResourceUsage.cumulativeConsumption, userResourceUsageEntity.get().cumulativeConsumption);

		long id = userResourceUsageEntity
			.map(usage -> usage.id)
			.orElse(0L);
		userResourceUsageEntityRepository.save(
			UserResourceUsageEntity.builder()
				.id(id)
				.projectId(userResourceUsage.projectId.id)
				.projectAllocationId(userResourceUsage.projectAllocationId.id)
				.fenixUserId(userResourceUsage.fenixUserId.id)
				.cumulativeConsumption(userResourceUsage.cumulativeConsumption)
				.consumedUntil(userResourceUsage.utcConsumedUntil)
				.build()
		);
	}

	@Override
	public ResourceUsageByCredit findResourceUsagesSumsBySiteId(SiteId siteId) {
		Map<ResourceCreditId, BigDecimal> map =
			resourceUsageEntityRepository.findAllBySiteId(siteId.id).stream()
				.collect(getResourceUsageSumCollector(resourceUsage -> new ResourceCreditId(resourceUsage.resourceCreditId)));
		return new ResourceUsageByCredit(map);
	}

	@Override
	public ResourceUsageByCommunityAllocation findResourceUsagesSumsByCommunityId(CommunityId communityId) {
		Map<CommunityAllocationId, BigDecimal> map =
			resourceUsageEntityRepository.findAllByCommunityId(communityId.id).stream()
			.collect(getResourceUsageSumCollector(resourceUsage -> new CommunityAllocationId(resourceUsage.communityAllocationId)));
		return new ResourceUsageByCommunityAllocation(map);
	}

	@Override
	public Set<UserResourceUsage> findUserResourceUsages(Set<ProjectAllocationId> projectAllocations, LocalDateTime from,
	                                                     LocalDateTime to) {
		Set<UUID> ids = projectAllocations.stream()
			.map(id -> id.id)
			.collect(Collectors.toSet());
		return userResourceUsageHistoryEntityRepository.findAllByProjectAllocationIdInAndInPeriod(ids, from, to)
				.stream().map(UserResourceUsageHistoryEntity::toUserResourceUsage)
				.collect(toSet());
	}

	@Override
	public Set<UserResourceUsage> findUserResourceUsagesHistory(ProjectAllocationId projectAllocationId) {
		return userResourceUsageHistoryEntityRepository.findAllByProjectAllocationId(projectAllocationId.id)
			.stream().map(UserResourceUsageHistoryEntity::toUserResourceUsage)
			.collect(toSet());
	}

	@Override
	public Set<ResourceUsage> findResourceUsagesHistory(ProjectAllocationId projectAllocationId) {
		return resourceUsageHistoryEntityRepository.findAllByProjectAllocationId(projectAllocationId.id).stream()
			.map(ResourceUsageHistoryEntity::toResourceUsage)
			.collect(toSet());
	}

	@Override
	public Set<ResourceUsage> findResourceUsagesHistoryByCommunityAllocationId(CommunityAllocationId communityAllocationId) {
		return resourceUsageHistoryEntityRepository.findAllByCommunityAllocationId(communityAllocationId.id).stream()
			.map(ResourceUsageHistoryEntity::toResourceUsage)
			.collect(toSet());
	}

	private static <T> Collector<ResourceUsageEntity, ?, Map<T, BigDecimal>> getResourceUsageSumCollector(
		Function<ResourceUsageEntity, T> classifier
	) {
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
	public Set<ResourceUsage> findCurrentResourceUsages(ProjectId projectId) {
		return resourceUsageEntityRepository.findAllByProjectId(projectId.id).stream()
			.map(ResourceUsageEntity::toResourceUsage)
			.collect(toSet());
	}

	@Override
	public Optional<ResourceUsage> findCurrentResourceUsage(ProjectAllocationId projectAllocationId) {
		return resourceUsageEntityRepository.findByProjectAllocationId(projectAllocationId.id)
			.map(ResourceUsageEntity::toResourceUsage);
	}
}
