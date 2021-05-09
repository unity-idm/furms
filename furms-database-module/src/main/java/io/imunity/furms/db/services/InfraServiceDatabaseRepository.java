/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.services;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.StringUtils.isEmpty;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.spi.services.InfraServiceRepository;

@Repository
class InfraServiceDatabaseRepository implements InfraServiceRepository {
	private final InfraServiceEntityRepository repository;

	InfraServiceDatabaseRepository(InfraServiceEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<InfraService> findById(String id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(UUID.fromString(id))
			.map(InfraServiceEntity::toService);
	}

	@Override
	public Set<InfraService> findAll(String siteId) {
		return repository.findAllBySiteId(UUID.fromString(siteId))
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
	public String create(InfraService infraService) {
		InfraServiceEntity savedService = repository.save(
			InfraServiceEntity.builder()
				.siteId(UUID.fromString(infraService.siteId))
				.name(infraService.name)
				.description(infraService.description)
				.build()
		);
		return savedService.getId().toString();
	}

	@Override
	public String update(InfraService infraService) {
		return repository.findById(UUID.fromString(infraService.id))
			.map(oldService -> InfraServiceEntity.builder()
				.id(oldService.getId())
				.siteId(UUID.fromString(infraService.siteId))
				.name(infraService.name)
				.description(infraService.description)
				.build()
			)
			.map(repository::save)
			.map(InfraServiceEntity::getId)
			.map(UUID::toString)
			.orElseThrow(() -> new IllegalStateException("Infra Service not found: " + infraService));
	}

	@Override
	public boolean exists(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(UUID.fromString(id));
	}

	@Override
	public boolean isNameUsed(String name, String siteId) {
		return repository.existsByNameAndSiteId(name, UUID.fromString(siteId));
	}
	
	@Override
	public void delete(String id) {
		repository.deleteById(UUID.fromString(id));
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
}
