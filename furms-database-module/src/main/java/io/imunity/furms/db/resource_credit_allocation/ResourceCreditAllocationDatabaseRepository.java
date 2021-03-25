/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credit_allocation;

import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocation;
import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocationExtend;
import io.imunity.furms.spi.resource_credit_allocation.ResourceCreditAllocationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.StringUtils.isEmpty;

@Repository
class ResourceCreditAllocationDatabaseRepository implements ResourceCreditAllocationRepository {
	private final ResourceCreditAllocationEntityRepository repository;
	private final ResourceCreditAllocationReadEntityRepository readRepository;

	ResourceCreditAllocationDatabaseRepository(ResourceCreditAllocationEntityRepository repository,
	                                           ResourceCreditAllocationReadEntityRepository readRepository) {
		this.repository = repository;
		this.readRepository = readRepository;
	}

	@Override
	public Optional<ResourceCreditAllocation> findById(String id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(UUID.fromString(id))
			.map(ResourceCreditAllocationEntity::toResourceCreditAllocation);
	}

	@Override
	public Optional<ResourceCreditAllocationExtend> findByIdWithRelatedObjects(String id) {
		if (isEmpty(id)) {
			return empty();
		}
		return readRepository.findById(UUID.fromString(id))
			.map(ResourceCreditAllocationReadEntity::toResourceCreditAllocation);
	}

	@Override
	public Set<ResourceCreditAllocationExtend> findAllWithRelatedObjects(String communityId) {
		return readRepository.findAllByCommunityId(UUID.fromString(communityId)).stream()
			.map(ResourceCreditAllocationReadEntity::toResourceCreditAllocation)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ResourceCreditAllocation> findAll() {
		return stream(repository.findAll().spliterator(), false)
			.map(ResourceCreditAllocationEntity::toResourceCreditAllocation)
			.collect(toSet());
	}

	@Override
	public String create(ResourceCreditAllocation service) {
		ResourceCreditAllocationEntity savedResourceCreditAllocation = repository.save(
			ResourceCreditAllocationEntity.builder()
				.siteId(UUID.fromString(service.siteId))
				.communityId(UUID.fromString(service.communityId))
				.resourceTypeId(UUID.fromString(service.resourceTypeId))
				.resourceCreditId(UUID.fromString(service.resourceCreditId))
				.name(service.name)
				.amount(service.amount)
				.build()
		);
		return savedResourceCreditAllocation.getId().toString();
	}

	@Override
	public String update(ResourceCreditAllocation service) {
		return repository.findById(UUID.fromString(service.id))
			.map(oldResourceCreditAllocation -> ResourceCreditAllocationEntity.builder()
				.id(oldResourceCreditAllocation.getId())
				.siteId(UUID.fromString(service.siteId))
				.communityId(UUID.fromString(service.communityId))
				.resourceTypeId(UUID.fromString(service.resourceTypeId))
				.resourceCreditId(UUID.fromString(service.resourceCreditId))
				.name(service.name)
				.amount(service.amount)
				.build()
			)
			.map(repository::save)
			.map(ResourceCreditAllocationEntity::getId)
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
	public boolean existsByResourceCreditId(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsByResourceCreditId(UUID.fromString(id));
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

