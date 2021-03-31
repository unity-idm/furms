/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credits;

import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.resource_credits.CreateResourceCreditEvent;
import io.imunity.furms.domain.resource_credits.RemoveResourceCreditEvent;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.UpdateResourceCreditEvent;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
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
class ResourceCreditServiceImpl implements ResourceCreditService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ResourceCreditRepository resourceCreditRepository;
	private final ResourceCreditServiceValidator validator;
	private final ApplicationEventPublisher publisher;

	public ResourceCreditServiceImpl(ResourceCreditRepository resourceCreditRepository, ResourceCreditServiceValidator validator, ApplicationEventPublisher publisher) {
		this.resourceCreditRepository = resourceCreditRepository;
		this.validator = validator;
		this.publisher = publisher;
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
	public Set<ResourceCredit> findAll() {
		return resourceCreditRepository.findAll();
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
}