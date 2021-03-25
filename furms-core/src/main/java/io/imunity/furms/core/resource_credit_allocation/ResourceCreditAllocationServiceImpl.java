/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credit_allocation;

import io.imunity.furms.api.resource_credit_allocation.ResourceCreditAllocationService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.resource_credit_allocation.*;
import io.imunity.furms.spi.resource_credit_allocation.ResourceCreditAllocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;

@Service
class ResourceCreditAllocationServiceImpl implements ResourceCreditAllocationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ResourceCreditAllocationRepository resourceCreditAllocationRepository;
	private final ResourceCreditAllocationServiceValidator validator;
	private final ApplicationEventPublisher publisher;

	public ResourceCreditAllocationServiceImpl(ResourceCreditAllocationRepository resourceCreditAllocationRepository, ResourceCreditAllocationServiceValidator validator, ApplicationEventPublisher publisher) {
		this.resourceCreditAllocationRepository = resourceCreditAllocationRepository;
		this.validator = validator;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public Optional<ResourceCreditAllocation> findById(String id) {
		return resourceCreditAllocationRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public Optional<ResourceCreditAllocationExtend> findByIdWithRelatedObjects(String id) {
		return resourceCreditAllocationRepository.findByIdWithRelatedObjects(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public Set<ResourceCreditAllocation> findAll() {
		return resourceCreditAllocationRepository.findAll();
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public Set<ResourceCreditAllocationExtend> findAllWithRelatedObjects(String communityId) {
		return resourceCreditAllocationRepository.findAllWithRelatedObjects(communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "resourceCreditAllocation.communityId")
	public void create(ResourceCreditAllocation resourceCreditAllocation) {
		validator.validateCreate(resourceCreditAllocation);
		String id = resourceCreditAllocationRepository.create(resourceCreditAllocation);
		publisher.publishEvent(new CreateResourceCreditAllocationEvent(resourceCreditAllocation.id));
		LOG.info("ResourceCreditAllocation with given ID: {} was created: {}", id, resourceCreditAllocation);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "resourceCreditAllocation.communityId")
	public void update(ResourceCreditAllocation resourceCreditAllocation) {
		validator.validateUpdate(resourceCreditAllocation);
		resourceCreditAllocationRepository.update(resourceCreditAllocation);
		publisher.publishEvent(new UpdateResourceCreditAllocationEvent(resourceCreditAllocation.id));
		LOG.info("ResourceCreditAllocation was updated {}", resourceCreditAllocation);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "id")
	public void delete(String id) {
		validator.validateDelete(id);
		resourceCreditAllocationRepository.delete(id);
		publisher.publishEvent(new RemoveResourceCreditAllocationEvent(id));
		LOG.info("ResourceCreditAllocation with given ID: {} was deleted", id);
	}
}
