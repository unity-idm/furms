/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.project_allocation.CreateProjectAllocationEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.RemoveProjectAllocationEvent;
import io.imunity.furms.domain.project_allocation.UpdateProjectAllocationEvent;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
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
class ProjectAllocationServiceImpl implements ProjectAllocationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectAllocationRepository projectAllocationRepository;
	private final ProjectAllocationServiceValidator validator;
	private final ApplicationEventPublisher publisher;

	ProjectAllocationServiceImpl(ProjectAllocationRepository projectAllocationRepository, ProjectAllocationServiceValidator validator, ApplicationEventPublisher publisher) {
		this.projectAllocationRepository = projectAllocationRepository;
		this.validator = validator;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public Optional<ProjectAllocation> findById(String id) {
		return projectAllocationRepository.findById(id);
	}

//	@Override
//	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
//	public Optional<ProjectAllocationResolved> findByIdWithRelatedObjects(String id) {
//		return projectAllocationRepository.findByIdWithRelatedObjects(id);
//	}
//	@Override
//	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
//	public Set<ProjectAllocationResolved> findAllWithRelatedObjects(String communityId) {
//		return projectAllocationRepository.findAllWithRelatedObjects(communityId);
//	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public Set<ProjectAllocation> findAll() {
		return projectAllocationRepository.findAll();
	}


	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityAllocation.communityId")
	public void create(ProjectAllocation communityAllocation) {
		validator.validateCreate(communityAllocation);
		String id = projectAllocationRepository.create(communityAllocation);
		publisher.publishEvent(new CreateProjectAllocationEvent(communityAllocation.id));
		LOG.info("ProjectAllocation with given ID: {} was created: {}", id, communityAllocation);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityAllocation.communityId")
	public void update(ProjectAllocation communityAllocation) {
		validator.validateUpdate(communityAllocation);
		projectAllocationRepository.update(communityAllocation);
		publisher.publishEvent(new UpdateProjectAllocationEvent(communityAllocation.id));
		LOG.info("ProjectAllocation was updated {}", communityAllocation);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "id")
	public void delete(String id) {
		validator.validateDelete(id);
		projectAllocationRepository.delete(id);
		publisher.publishEvent(new RemoveProjectAllocationEvent(id));
		LOG.info("ProjectAllocation with given ID: {} was deleted", id);
	}
}
