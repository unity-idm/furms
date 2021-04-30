/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credits;

import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.resource_credits.CreateResourceCreditEvent;
import io.imunity.furms.domain.resource_credits.RemoveResourceCreditEvent;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditFenixDashboard;
import io.imunity.furms.domain.resource_credits.UpdateResourceCreditEvent;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toSet;

@Service
class ResourceCreditServiceImpl implements ResourceCreditService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ResourceCreditRepository resourceCreditRepository;
	private final ResourceCreditServiceValidator validator;
	private final ApplicationEventPublisher publisher;
	private final CommunityAllocationService communityAllocationService;

	public ResourceCreditServiceImpl(ResourceCreditRepository resourceCreditRepository,
	                                 ResourceCreditServiceValidator validator,
	                                 ApplicationEventPublisher publisher,
	                                 CommunityAllocationService communityAllocationService) {
		this.resourceCreditRepository = resourceCreditRepository;
		this.validator = validator;
		this.publisher = publisher;
		this.communityAllocationService = communityAllocationService;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "id")
	public Optional<ResourceCredit> findById(String id) {
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
	public Set<ResourceCreditFenixDashboard> findAllForFenixAdminDashboard(String name, boolean fullyDistributed, boolean includedExpired) {
		return resourceCreditRepository.findAllByNameAndIncludedExpired(name, includedExpired).stream()
				.map(credit -> ResourceCreditFenixDashboard.builder()
						.id(credit.id)
						.name(credit.name)
						.siteId(credit.siteId)
						.resourceTypeId(credit.resourceTypeId)
						.split(credit.split)
						.access(credit.access)
						.amount(credit.amount)
						.remaining(communityAllocationService.getAvailableAmount(credit.id))
						.utcCreateTime(credit.utcCreateTime)
						.utcStartTime(credit.utcStartTime)
						.utcEndTime(credit.utcEndTime)
						.build())
				.filter(credit -> fullyDistributedFilter(credit.remaining, fullyDistributed))
				.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "resourceCredit.id")
	public void create(ResourceCredit resourceCredit) {
		validator.validateCreate(resourceCredit);
		String id = resourceCreditRepository.create(resourceCredit);
		publisher.publishEvent(new CreateResourceCreditEvent(resourceCredit.id));
		LOG.info("ResourceCredit with given ID: {} was created: {}", id, resourceCredit);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "resourceCredit.id")
	public void update(ResourceCredit resourceCredit) {
		validator.validateUpdate(resourceCredit);
		resourceCreditRepository.update(resourceCredit);
		publisher.publishEvent(new UpdateResourceCreditEvent(resourceCredit.id));
		LOG.info("ResourceCredit was updated {}", resourceCredit);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "id")
	public void delete(String id) {
		validator.validateDelete(id);
		resourceCreditRepository.delete(id);
		publisher.publishEvent(new RemoveResourceCreditEvent(id));
		LOG.info("ResourceCredit with given ID: {} was deleted", id);
	}

	private boolean fullyDistributedFilter(BigDecimal availableAmount, boolean fullyDistributed) {
		return availableAmount.compareTo(ZERO) != 0
				|| (availableAmount.compareTo(ZERO) == 0) == fullyDistributed;
	}
}
