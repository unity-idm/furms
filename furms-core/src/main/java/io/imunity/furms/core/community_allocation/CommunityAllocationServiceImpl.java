/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.community_allocation;

import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.community_allocation.CommunityAllocationCreatedEvent;
import io.imunity.furms.domain.community_allocation.CommunityAllocationRemovedEvent;
import io.imunity.furms.domain.community_allocation.CommunityAllocationUpdatedEvent;
import io.imunity.furms.domain.resource_usage.ResourceUsageByCommunityAllocation;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.imunity.furms.core.utils.ResourceCreditsUtils.includedFullyDistributedFilter;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static java.util.stream.Collectors.toSet;

@Service
class CommunityAllocationServiceImpl implements CommunityAllocationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final CommunityAllocationRepository communityAllocationRepository;
	private final CommunityAllocationServiceValidator validator;
	private final ApplicationEventPublisher publisher;
	private final ProjectAllocationService projectAllocationService;
	private final ResourceUsageRepository resourceUsageRepository;

	CommunityAllocationServiceImpl(CommunityAllocationRepository communityAllocationRepository,
	                               CommunityAllocationServiceValidator validator,
	                               ApplicationEventPublisher publisher,
	                               ProjectAllocationService projectAllocationService,
	                               ResourceUsageRepository resourceUsageRepository) {
		this.communityAllocationRepository = communityAllocationRepository;
		this.validator = validator;
		this.publisher = publisher;
		this.projectAllocationService = projectAllocationService;
		this.resourceUsageRepository = resourceUsageRepository;
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY)
	public Optional<CommunityAllocation> findById(String id) {
		return communityAllocationRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY)
	public Optional<CommunityAllocationResolved> findByIdWithRelatedObjects(String id) {
		return communityAllocationRepository.findByIdWithRelatedObjects(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Optional<CommunityAllocationResolved> findByCommunityIdAndIdWithRelatedObjects(String communityId, String id) {
		return communityAllocationRepository.findByIdWithRelatedObjects(id)
				.map(credit -> credit.copyBuilder()
						.remaining(projectAllocationService.getAvailableAmount(communityId, credit.id))
						.consumed(resourceUsageRepository.findResourceUsagesSumsByCommunityId(communityId).get(credit.id))
						.build());
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY)
	public Set<CommunityAllocation> findAll() {
		return communityAllocationRepository.findAll();
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<CommunityAllocation> findAllByCommunityId(String communityId) {
		return communityAllocationRepository.findAllByCommunityId(communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<String> getOccupiedNames(String communityId) {
		return communityAllocationRepository.findAllByCommunityId(communityId).stream()
			.map(communityAllocation -> communityAllocation.name)
			.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<CommunityAllocationResolved> findAllWithRelatedObjects(String communityId) {
		ResourceUsageByCommunityAllocation resourceUsageSum = resourceUsageRepository.findResourceUsagesSumsByCommunityId(communityId);
		return communityAllocationRepository.findAllByCommunityIdWithRelatedObjects(communityId).stream()
			.map(credit -> credit.copyBuilder()
				.remaining(projectAllocationService.getAvailableAmount(communityId, credit.id))
				.consumed(resourceUsageSum.get(credit.id))
				.build())
			.collect(Collectors.toSet());
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<CommunityAllocationResolved> findAllWithRelatedObjects(String communityId,
	                                                                  String name,
	                                                                  boolean includedFullyDistributed,
	                                                                  boolean includedExpired) {
		final Set<CommunityAllocationResolved> communityAllocations = includedExpired
				? communityAllocationRepository.findAllByCommunityIdAndNameOrSiteNameWithRelatedObjects(communityId, name)
				: communityAllocationRepository.findAllNotExpiredByCommunityIdAndNameOrSiteNameWithRelatedObjects(communityId, name);
		return communityAllocations.stream()
				.map(credit -> CommunityAllocationResolved.builder()
					.id(credit.id)
					.site(credit.site)
					.resourceType(credit.resourceType)
					.resourceCredit(credit.resourceCredit)
					.communityId(credit.communityId)
					.name(credit.name)
					.amount(credit.amount)
					.remaining(projectAllocationService.getAvailableAmount(communityId, credit.id))
					.build())
				.filter(credit -> includedFullyDistributedFilter(credit.remaining, includedFullyDistributed))
				.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<CommunityAllocationResolved> findAllNotExpiredByCommunityIdWithRelatedObjects(String communityId) {
		return communityAllocationRepository.findAllNotExpiredByCommunityIdWithRelatedObjects(communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY)
	public BigDecimal getAvailableAmountForNew(String resourceCreditId) {
		return communityAllocationRepository.getAvailableAmount(resourceCreditId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY)
	public BigDecimal getAvailableAmountForUpdate(String resourceCreditId, String communityAllocationId) {
		BigDecimal free = communityAllocationRepository.getAvailableAmount(resourceCreditId);
		BigDecimal currentlyAllocated = communityAllocationRepository.findById(communityAllocationId).get().amount;
		return free.add(currentlyAllocated);
	}
	
	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public void create(CommunityAllocation communityAllocation) {
		validator.validateCreate(communityAllocation);
		String id = communityAllocationRepository.create(communityAllocation);
		CommunityAllocation allocation = communityAllocationRepository.findById(id).get();
		publisher.publishEvent(new CommunityAllocationCreatedEvent(allocation));
		LOG.info("CommunityAllocation with given ID: {} was created: {}", id, communityAllocation);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public void update(CommunityAllocation communityAllocation) {
		validator.validateUpdate(communityAllocation);
		CommunityAllocation oldAllocation = communityAllocationRepository.findById(communityAllocation.id).get();
		communityAllocationRepository.update(communityAllocation);
		publisher.publishEvent(new CommunityAllocationUpdatedEvent(oldAllocation, communityAllocation));
		LOG.info("CommunityAllocation was updated {}", communityAllocation);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public void delete(String id) {
		validator.validateDelete(id);
		CommunityAllocation allocation = communityAllocationRepository.findById(id).get();
		communityAllocationRepository.delete(id);
		publisher.publishEvent(new CommunityAllocationRemovedEvent(allocation));
		LOG.info("CommunityAllocation with given ID: {} was deleted", id);
	}
}
