/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_types;


import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.StringUtils.isEmpty;

@Repository
class ResourceTypeDatabaseRepository implements ResourceTypeRepository {
	private final ResourceTypeEntityRepository repository;

	ResourceTypeDatabaseRepository(ResourceTypeEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<ResourceType> findById(String id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(UUID.fromString(id))
			.map(ResourceTypeEntity::toResourceType);
	}

	@Override
	public Set<ResourceType> findAllBySiteId(String siteId) {
		return repository.findAllBySiteId(UUID.fromString(siteId))
			.map(ResourceTypeEntity::toResourceType)
			.collect(toSet());
	}

	@Override
	public Set<ResourceType> findAllByInfraServiceId(String siteId) {
		return repository.findAllByServiceId(UUID.fromString(siteId))
			.map(ResourceTypeEntity::toResourceType)
			.collect(toSet());
	}

	@Override
	public Set<ResourceType> findAll() {
		return stream(repository.findAll().spliterator(), false)
			.map(ResourceTypeEntity::toResourceType)
			.collect(toSet());
	}

	@Override
	public String create(ResourceType service) {
		ResourceTypeEntity savedResourceType = repository.save(
			ResourceTypeEntity.builder()
				.siteId(UUID.fromString(service.siteId))
				.serviceId(UUID.fromString(service.serviceId))
				.name(service.name)
				.type(service.type)
				.unit(service.unit)
				.build()
		);
		return savedResourceType.getId().toString();
	}

	@Override
	public String update(ResourceType service) {
		return repository.findById(UUID.fromString(service.id))
			.map(oldResourceType -> ResourceTypeEntity.builder()
				.id(oldResourceType.getId())
				.siteId(UUID.fromString(service.siteId))
				.serviceId(UUID.fromString(service.serviceId))
				.name(service.name)
				.type(service.type)
				.unit(service.unit)
				.build()
			)
			.map(repository::save)
			.map(ResourceTypeEntity::getId)
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
