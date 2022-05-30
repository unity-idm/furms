/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_types;

import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.resource_types.ResourceTypeCreatedEvent;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.resource_types.ResourceTypeRemovedEvent;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeUpdatedEvent;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;

@Service
class ResourceTypeServiceImpl implements ResourceTypeService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ResourceTypeRepository resourceTypeRepository;
	private final ResourceTypeServiceValidator validator;
	private final ApplicationEventPublisher publisher;

	public ResourceTypeServiceImpl(ResourceTypeRepository resourceTypeRepository, ResourceTypeServiceValidator validator, ApplicationEventPublisher publisher) {
		this.resourceTypeRepository = resourceTypeRepository;
		this.validator = validator;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Optional<ResourceType> findById(ResourceTypeId id, SiteId siteId) {
		return resourceTypeRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<ResourceType> findAll(SiteId siteId) {
		return resourceTypeRepository.findAllBySiteId(siteId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE)
	public Set<ResourceType> findAll() {
		return resourceTypeRepository.findAll();
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "resourceType.siteId")
	public void create(ResourceType resourceType) {
		validator.validateCreate(resourceType);
		ResourceTypeId id = resourceTypeRepository.create(resourceType);
		ResourceType created = resourceTypeRepository.findById(id).get();
		publisher.publishEvent(new ResourceTypeCreatedEvent(created));
		LOG.info("ResourceType with given ID: {} was created: {}", id, resourceType);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "resourceType.siteId")
	public void update(ResourceType resourceType) {
		validator.validateUpdate(resourceType);
		ResourceType oldResourceType = resourceTypeRepository.findById(resourceType.id).get();
		resourceTypeRepository.update(resourceType);
		publisher.publishEvent(new ResourceTypeUpdatedEvent(oldResourceType, resourceType));
		LOG.info("ResourceType was updated {}", resourceType);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "siteId")
	public void delete(ResourceTypeId id, SiteId siteId) {
		validator.validateDelete(id);
		ResourceType resourceType = resourceTypeRepository.findById(id).get();
		resourceTypeRepository.delete(id);
		publisher.publishEvent(new ResourceTypeRemovedEvent(resourceType));
		LOG.info("ResourceType with given ID: {} was deleted", id);
	}
}
