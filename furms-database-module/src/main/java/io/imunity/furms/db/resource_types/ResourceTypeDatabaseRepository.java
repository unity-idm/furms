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
	public String create(ResourceType resourceType) {
		ResourceTypeEntity savedResourceType = repository.save(
			ResourceTypeEntity.builder()
				.siteId(UUID.fromString(resourceType.siteId))
				.serviceId(UUID.fromString(resourceType.serviceId))
				.name(resourceType.name)
				.type(resourceType.type)
				.unit(resourceType.unit)
				.accessible(resourceType.accessibleForAllProjectMembers)
				.build()
		);
		return savedResourceType.getId().toString();
	}

	@Override
	public String update(ResourceType resourceType) {
		return repository.findById(UUID.fromString(resourceType.id))
			.map(oldResourceType -> ResourceTypeEntity.builder()
				.id(oldResourceType.getId())
				.siteId(UUID.fromString(resourceType.siteId))
				.serviceId(UUID.fromString(resourceType.serviceId))
				.name(resourceType.name)
				.type(resourceType.type)
				.unit(resourceType.unit)
				.accessible(resourceType.accessibleForAllProjectMembers)
				.build()
			)
			.map(repository::save)
			.map(ResourceTypeEntity::getId)
			.map(UUID::toString)
			.orElseThrow(() -> new IllegalStateException("Resource type not found: " + resourceType));
	}

	@Override
	public boolean exists(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(UUID.fromString(id));
	}

	@Override
	public boolean isNamePresent(String name, String siteId) {
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
