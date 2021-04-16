/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.community_allocation;

import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.community_allocation.*;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;

@Service
class CommunityAllocationServiceImpl implements CommunityAllocationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final CommunityAllocationRepository communityAllocationRepository;
	private final CommunityAllocationServiceValidator validator;
	private final ApplicationEventPublisher publisher;

	CommunityAllocationServiceImpl(CommunityAllocationRepository communityAllocationRepository, CommunityAllocationServiceValidator validator, ApplicationEventPublisher publisher) {
		this.communityAllocationRepository = communityAllocationRepository;
		this.validator = validator;
		this.publisher = publisher;
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
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY)
	public Set<CommunityAllocation> findAll() {
		return communityAllocationRepository.findAll();
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY)
	public Set<CommunityAllocationResolved> findAllWithRelatedObjects(String communityId) {
		return communityAllocationRepository.findAllWithRelatedObjects(communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY)
	public BigDecimal getAvailableAmount(String resourceCreditId) {
		return communityAllocationRepository.getAvailableAmount(resourceCreditId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public void create(CommunityAllocation communityAllocation) {
		validator.validateCreate(communityAllocation);
		String id = communityAllocationRepository.create(communityAllocation);
		publisher.publishEvent(new CreateCommunityAllocationEvent(communityAllocation.id));
		LOG.info("CommunityAllocation with given ID: {} was created: {}", id, communityAllocation);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public void update(CommunityAllocation communityAllocation) {
		validator.validateUpdate(communityAllocation);
		communityAllocationRepository.update(communityAllocation);
		publisher.publishEvent(new UpdateCommunityAllocationEvent(communityAllocation.id));
		LOG.info("CommunityAllocation was updated {}", communityAllocation);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public void delete(String id) {
		validator.validateDelete(id);
		communityAllocationRepository.delete(id);
		publisher.publishEvent(new RemoveCommunityAllocationEvent(id));
		LOG.info("CommunityAllocation with given ID: {} was deleted", id);
	}
}
