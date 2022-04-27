/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_types;

import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

@Repository
class ResourceTypeDatabaseRepository implements ResourceTypeRepository {
	private final ResourceTypeEntityRepository repository;
	private final ResourceTypeConverter resourceTypeConverter;

	ResourceTypeDatabaseRepository(ResourceTypeEntityRepository repository, ResourceTypeConverter resourceTypeConverter) {
		this.repository = repository;
		this.resourceTypeConverter = resourceTypeConverter;
	}

	@Override
	public Optional<ResourceType> findById(ResourceTypeId id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(id.id)
			.map(resourceTypeConverter::toResourceType);
	}

	@Override
	public Set<ResourceType> findAllBySiteId(SiteId siteId) {
		return repository.findAllBySiteId(siteId.id).stream()
			.map(resourceTypeConverter::toResourceType)
			.collect(toSet());
	}

	@Override
	public Set<ResourceType> findAllByInfraServiceId(InfraServiceId serviceId) {
		return repository.findAllByServiceId(serviceId.id).stream()
			.map(resourceTypeConverter::toResourceType)
			.collect(toSet());
	}

	@Override
	public Set<ResourceType> findAll() {
		return stream(repository.findAll().spliterator(), false)
			.map(resourceTypeConverter::toResourceType)
			.collect(toSet());
	}

	@Override
	public ResourceTypeId create(ResourceType resourceType) {
		ResourceTypeEntity savedResourceType = repository.save(
			ResourceTypeEntity.builder()
				.siteId(resourceType.siteId.id)
				.serviceId(resourceType.serviceId.id)
				.name(resourceType.name)
				.type(resourceType.type)
				.unit(resourceType.unit)
				.accessible(resourceType.accessibleForAllProjectMembers)
				.build()
		);
		return new ResourceTypeId(savedResourceType.getId());
	}

	@Override
	public void update(ResourceType resourceType) {
		repository.findById(resourceType.id.id)
			.map(oldResourceType -> ResourceTypeEntity.builder()
				.id(oldResourceType.getId())
				.siteId(resourceType.siteId.id)
				.serviceId(resourceType.serviceId.id)
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
	public boolean exists(ResourceTypeId id) {
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
	public void delete(ResourceTypeId id) {
		repository.deleteById(id.id);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	private boolean isEmpty(ResourceTypeId id) {
		return id == null || id.id == null;
	}
}
