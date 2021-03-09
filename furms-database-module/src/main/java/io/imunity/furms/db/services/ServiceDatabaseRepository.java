/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.services;

import io.imunity.furms.domain.services.Service;
import io.imunity.furms.spi.services.ServiceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.StringUtils.isEmpty;

@Repository
public class ServiceDatabaseRepository implements ServiceRepository {
	private final ServiceEntityRepository repository;

	ServiceDatabaseRepository(ServiceEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Service> findById(String id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(UUID.fromString(id))
			.map(ServiceEntity::toService);
	}

	@Override
	public Set<Service> findAll(String siteId) {
		return repository.findAllBySiteId(UUID.fromString(siteId))
			.map(ServiceEntity::toService)
			.collect(toSet());
	}

	@Override
	public Set<Service> findAll() {
		return stream(repository.findAll().spliterator(), false)
			.map(ServiceEntity::toService)
			.collect(toSet());
	}

	@Override
	public String create(Service service) {
		ServiceEntity savedService = repository.save(
			ServiceEntity.builder()
				.siteId(UUID.fromString(service.siteId))
				.name(service.name)
				.description(service.description)
				.build()
		);
		return savedService.getId().toString();
	}

	@Override
	public String update(Service service) {
		return repository.findById(UUID.fromString(service.id))
			.map(oldService -> ServiceEntity.builder()
				.id(oldService.getId())
				.siteId(UUID.fromString(service.siteId))
				.name(service.name)
				.description(service.description)
				.build()
			)
			.map(repository::save)
			.map(ServiceEntity::getId)
			.map(UUID::toString)
			.get();
	}

	@Override
	public boolean exists(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(UUID.fromString(id));
	}

	@Override
	public boolean isUniqueName(String name) {
		return !repository.existsByName(name);
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
