/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.services;

import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.services.InfraServiceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

@Repository
class InfraServiceDatabaseRepository implements InfraServiceRepository {
	private final InfraServiceEntityRepository repository;

	InfraServiceDatabaseRepository(InfraServiceEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<InfraService> findById(InfraServiceId id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(id.id)
			.map(InfraServiceEntity::toService);
	}

	@Override
	public Set<InfraService> findAll(SiteId siteId) {
		return repository.findAllBySiteId(siteId.id).stream()
			.map(InfraServiceEntity::toService)
			.collect(toSet());
	}

	@Override
	public Set<InfraService> findAll() {
		return stream(repository.findAll().spliterator(), false)
			.map(InfraServiceEntity::toService)
			.collect(toSet());
	}

	@Override
	public InfraServiceId create(InfraService infraService) {
		InfraServiceEntity savedService = repository.save(
			InfraServiceEntity.builder()
				.siteId(infraService.siteId.id)
				.name(infraService.name)
				.description(infraService.description)
				.policyId(infraService.policyId.id)
				.build()
		);
		return new InfraServiceId(savedService.getId());
	}

	@Override
	public void update(InfraService infraService) {
		repository.findById(infraService.id.id)
			.map(oldService -> InfraServiceEntity.builder()
				.id(oldService.getId())
				.siteId(infraService.siteId.id)
				.name(infraService.name)
				.description(infraService.description)
				.policyId(infraService.policyId.id)
				.build()
			)
			.map(repository::save)
			.map(InfraServiceEntity::getId)
			.map(UUID::toString)
			.orElseThrow(() -> new IllegalStateException("Infra Service not found: " + infraService));
	}

	@Override
	public boolean exists(InfraServiceId id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(id.id);
	}

	@Override
	public boolean isNamePresent(String name, SiteId siteId) {
		return repository.existsByNameAndSiteId(name, siteId.id);
	}
	
	@Override
	public void delete(InfraServiceId id) {
		repository.deleteById(id.id);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	private boolean isEmpty(InfraServiceId id) {
		return id == null || id.id == null;
	}
}
