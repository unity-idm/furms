/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credits;

import static io.imunity.furms.core.utils.ResourceCreditsUtils.includedFullyDistributedFilter;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static java.util.stream.Collectors.toSet;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.resource_credits.CreateResourceCreditEvent;
import io.imunity.furms.domain.resource_credits.RemoveResourceCreditEvent;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditWithAllocations;
import io.imunity.furms.domain.resource_credits.UpdateResourceCreditEvent;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;

@Service
class ResourceCreditServiceImpl implements ResourceCreditService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ResourceCreditRepository resourceCreditRepository;
	private final ResourceCreditServiceValidator validator;
	private final ApplicationEventPublisher publisher;
	private final CommunityAllocationService communityAllocationService;
	private final AuthzService authzService;
	private final ResourceTypeService resourceTypeService;

	public ResourceCreditServiceImpl(ResourceCreditRepository resourceCreditRepository,
	                                 ResourceCreditServiceValidator validator,
	                                 ApplicationEventPublisher publisher,
	                                 CommunityAllocationService communityAllocationService,
	                                 AuthzService authzService,
	                                 ResourceTypeService resourceTypeService) {
		this.resourceCreditRepository = resourceCreditRepository;
		this.validator = validator;
		this.publisher = publisher;
		this.communityAllocationService = communityAllocationService;
		this.authzService = authzService;
		this.resourceTypeService = resourceTypeService;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Optional<ResourceCredit> findById(String id, String siteId) {
		return resourceCreditRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<ResourceCredit> findAll(String siteId) {
		return resourceCreditRepository.findAll(siteId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE)
	public Set<ResourceCredit> findAllByResourceTypeId(String resourceTypeId) {
		return resourceCreditRepository.findAllByResourceTypeId(resourceTypeId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE)
	public Set<ResourceCredit> findAll() {
		return resourceCreditRepository.findAll();
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE)
	public Set<ResourceCreditWithAllocations> findAllWithAllocations(String name,
	                                                                 boolean includedFullyDistributed,
	                                                                 boolean includedExpired) {
		final Set<ResourceCredit> resourceCredits = includedExpired
				? resourceCreditRepository.findAllByNameOrSiteName(name)
				: resourceCreditRepository.findAllByNameOrSiteNameWithoutExpired(name);
		return resourceCredits.stream().map(credit -> ResourceCreditWithAllocations.builder()
					.id(credit.id)
					.name(credit.name)
					.siteId(credit.siteId)
					.resourceType(resourceTypeService.findById(credit.resourceTypeId, credit.siteId)
							.orElse(null))
					.split(credit.splittable)
					.amount(credit.amount)
					.remaining(communityAllocationService.getAvailableAmountForNew(credit.id))
					.utcCreateTime(credit.utcCreateTime)
					.utcStartTime(credit.utcStartTime)
					.utcEndTime(credit.utcEndTime)
					.build())
			.filter(credit -> includedFullyDistributedFilter(credit.getRemaining(), includedFullyDistributed))
			.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "resourceCredit.siteId")
	public void create(ResourceCredit resourceCredit) {
		validator.validateCreate(resourceCredit);
		String id = resourceCreditRepository.create(resourceCredit);
		publisher.publishEvent(new CreateResourceCreditEvent(id, authzService.getCurrentUserId()));
		LOG.info("ResourceCredit with given ID: {} was created: {}", id, resourceCredit);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "resourceCredit.siteId")
	public void update(ResourceCredit resourceCredit) {
		validator.validateUpdate(resourceCredit);
		resourceCreditRepository.update(resourceCredit);
		publisher.publishEvent(new UpdateResourceCreditEvent(resourceCredit.id));
		LOG.info("ResourceCredit was updated {}", resourceCredit);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "siteId")
	public void delete(String id, String siteId) {
		validator.validateDelete(id);
		resourceCreditRepository.delete(id);
		publisher.publishEvent(new RemoveResourceCreditEvent(id));
		LOG.info("ResourceCredit with given ID: {} was deleted", id);
	}
}
