/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.community_allocation;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.StringUtils.isEmpty;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;

@Repository
class CommunityAllocationDatabaseRepository implements CommunityAllocationRepository {
	private final CommunityAllocationEntityRepository repository;
	private final CommunityAllocationReadEntityRepository readRepository;

	CommunityAllocationDatabaseRepository(CommunityAllocationEntityRepository repository,
	                                      CommunityAllocationReadEntityRepository readRepository) {
		this.repository = repository;
		this.readRepository = readRepository;
	}

	@Override
	public Optional<CommunityAllocation> findById(String id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(UUID.fromString(id))
			.map(CommunityAllocationEntity::toCommunityAllocation);
	}

	@Override
	public Optional<CommunityAllocationResolved> findByIdWithRelatedObjects(String id) {
		if (isEmpty(id)) {
			return empty();
		}
		return readRepository.findById(UUID.fromString(id))
			.map(CommunityAllocationReadEntity::toCommunityAllocation);
	}

	@Override
	public Set<CommunityAllocationResolved> findAllByCommunityIdWithRelatedObjects(String communityId) {
		return readRepository.findAllByCommunityId(UUID.fromString(communityId)).stream()
			.map(CommunityAllocationReadEntity::toCommunityAllocation)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<CommunityAllocationResolved> findAllByCommunityIdAndNameOrSiteNameWithRelatedObjects(String communityId,
	                                                                                                String name) {
		return readRepository.findAllByCommunityIdAndNameOrSiteName(UUID.fromString(communityId), name).stream()
				.map(CommunityAllocationReadEntity::toCommunityAllocation)
				.collect(Collectors.toSet());
	}

	@Override
	public Set<CommunityAllocationResolved> findAllNotExpiredByCommunityIdAndNameOrSiteNameWithRelatedObjects(String communityId,
	                                                                                                          String name) {
		return readRepository.findAllNotExpiredByCommunityIdAndNameOrSiteName(UUID.fromString(communityId), name).stream()
				.map(CommunityAllocationReadEntity::toCommunityAllocation)
				.collect(Collectors.toSet());
	}

	@Override
	public BigDecimal getAvailableAmount(String resourceCreditId) {
		return readRepository.calculateAvailableAmount(UUID.fromString(resourceCreditId)).getAmount();
	}

	@Override
	public Set<CommunityAllocation> findAll() {
		return stream(repository.findAll().spliterator(), false)
			.map(CommunityAllocationEntity::toCommunityAllocation)
			.collect(toSet());
	}

	@Override
	public String create(CommunityAllocation allocation) {
		CommunityAllocationEntity savedCommunityAllocation = repository.save(
			CommunityAllocationEntity.builder()
				.communityId(UUID.fromString(allocation.communityId))
				.resourceCreditId(UUID.fromString(allocation.resourceCreditId))
				.name(allocation.name)
				.amount(allocation.amount)
				.build()
		);
		return savedCommunityAllocation.getId().toString();
	}

	@Override
	public String update(CommunityAllocation allocation) {
		return repository.findById(UUID.fromString(allocation.id))
			.map(oldCommunityAllocation -> CommunityAllocationEntity.builder()
				.id(oldCommunityAllocation.getId())
				.communityId(UUID.fromString(allocation.communityId))
				.resourceCreditId(UUID.fromString(allocation.resourceCreditId))
				.name(allocation.name)
				.amount(allocation.amount)
				.build()
			)
			.map(repository::save)
			.map(CommunityAllocationEntity::getId)
			.map(UUID::toString)
			.orElseThrow(() -> new IllegalStateException("Community allocation not found: " + allocation.id));
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

