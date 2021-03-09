/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import io.imunity.furms.api.services.ServiceService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.services.CreateServiceEvent;
import io.imunity.furms.domain.services.RemoveServiceEvent;
import io.imunity.furms.domain.services.Service;
import io.imunity.furms.domain.services.UpdateServiceEvent;
import io.imunity.furms.spi.services.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;

@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ServiceRepository serviceRepository;
	private final ServiceServiceValidator validator;
	private final ApplicationEventPublisher publisher;

	public ServiceServiceImpl(ServiceRepository serviceRepository, ServiceServiceValidator validator, ApplicationEventPublisher publisher) {
		this.serviceRepository = serviceRepository;
		this.validator = validator;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "id")
	public Optional<Service> findById(String id) {
		return serviceRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<Service> findAll(String siteId) {
		return serviceRepository.findAll(siteId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE)
	public Set<Service> findAll() {
		return serviceRepository.findAll();
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "service.id")
	public void create(Service service) {
		validator.validateCreate(service);
		String id = serviceRepository.create(service);
		publisher.publishEvent(new CreateServiceEvent(service.id));
		LOG.info("Project with given ID: {} was created: {}", id, service);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "service.id")
	public void update(Service service) {
		validator.validateUpdate(service);
		serviceRepository.update(service);
		publisher.publishEvent(new UpdateServiceEvent(service.id));
		LOG.info("Project was updated {}", service);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "id")
	public void delete(String id) {
		validator.validateDelete(id);
		serviceRepository.delete(id);
		publisher.publishEvent(new RemoveServiceEvent(id));
		LOG.info("Project with given ID: {} was deleted", id);
	}
}
