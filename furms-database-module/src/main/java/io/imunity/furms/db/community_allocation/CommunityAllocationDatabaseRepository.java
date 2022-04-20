/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.community_allocation;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static java.util.Optional.empty;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.ObjectUtils.isEmpty;

@Repository
class CommunityAllocationDatabaseRepository implements CommunityAllocationRepository {
	private final CommunityAllocationEntityRepository repository;
	private final CommunityAllocationReadEntityRepository readRepository;
	private final CommunityAllocationConverter communityAllocationConverter;

	CommunityAllocationDatabaseRepository(CommunityAllocationEntityRepository repository,
	                                      CommunityAllocationReadEntityRepository readRepository,
	                                      CommunityAllocationConverter communityAllocationConverter) {
		this.repository = repository;
		this.readRepository = readRepository;
		this.communityAllocationConverter = communityAllocationConverter;
	}

	@Override
	public Optional<CommunityAllocation> findById(CommunityAllocationId id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(id.id)
			.map(CommunityAllocationEntity::toCommunityAllocation);
	}

	@Override
	public Optional<CommunityAllocationResolved> findByIdWithRelatedObjects(CommunityAllocationId id) {
		if (isEmpty(id)) {
			return empty();
		}
		return readRepository.findById(id.id)
			.map(communityAllocationConverter::toCommunityAllocationResolved);
	}

	@Override
	public Set<CommunityAllocationResolved> findAllByCommunityIdWithRelatedObjects(CommunityId communityId) {
		return readRepository.findAllByCommunityId(communityId.id).stream()
			.map(communityAllocationConverter::toCommunityAllocationResolved)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<CommunityAllocation> findAllByCommunityId(CommunityId communityId) {
		return readRepository.findAllByCommunityId(communityId.id).stream()
				.map(communityAllocationConverter::toCommunityAllocation)
				.collect(Collectors.toSet());
	}

	@Override
	public Set<CommunityAllocationResolved> findAllNotExpiredByCommunityIdWithRelatedObjects(CommunityId communityId) {
		return readRepository.findAllByCommunityId(communityId.id).stream()
				.filter(not(CommunityAllocationReadEntity::isExpired))
				.map(communityAllocationConverter::toCommunityAllocationResolved)
				.collect(Collectors.toSet());
	}

	@Override
	public Set<CommunityAllocationResolved> findAllByCommunityIdAndNameOrSiteNameWithRelatedObjects(CommunityId communityId,
	                                                                                                String name) {
		return readRepository.findAllByCommunityIdAndNameOrSiteName(communityId.id, name).stream()
				.map(communityAllocationConverter::toCommunityAllocationResolved)
				.collect(Collectors.toSet());
	}

	@Override
	public Set<CommunityAllocationResolved> findAllNotExpiredByCommunityIdAndNameOrSiteNameWithRelatedObjects(CommunityId communityId,
	                                                                                                          String name) {
		return readRepository.findAllByCommunityIdAndNameOrSiteName(communityId.id, name).stream()
				.filter(not(CommunityAllocationReadEntity::isExpired))
				.map(communityAllocationConverter::toCommunityAllocationResolved)
				.collect(Collectors.toSet());
	}

	@Override
	public BigDecimal getAvailableAmount(ResourceCreditId resourceCreditId) {
		return readRepository.calculateAvailableAmount(resourceCreditId.id).getAmount();
	}

	@Override
	public Set<CommunityAllocation> findAll() {
		return stream(repository.findAll().spliterator(), false)
			.map(CommunityAllocationEntity::toCommunityAllocation)
			.collect(toSet());
	}

	@Override
	public CommunityAllocationId create(CommunityAllocation allocation) {
		CommunityAllocationEntity savedCommunityAllocation = repository.save(
			CommunityAllocationEntity.builder()
				.communityId(allocation.communityId.id)
				.resourceCreditId(allocation.resourceCreditId.id)
				.name(allocation.name)
				.amount(allocation.amount)
				.creationTime(convertToUTCTime(ZonedDateTime.now()))
				.build()
		);
		return new CommunityAllocationId(savedCommunityAllocation.getId());
	}

	@Override
	public void update(CommunityAllocation allocation) {
		repository.findById(allocation.id.id)
			.map(oldCommunityAllocation -> CommunityAllocationEntity.builder()
				.id(oldCommunityAllocation.getId())
				.communityId(allocation.communityId.id)
				.resourceCreditId(allocation.resourceCreditId.id)
				.name(allocation.name)
				.amount(allocation.amount)
				.creationTime(oldCommunityAllocation.creationTime)
				.build()
			)
			.map(repository::save)
			.map(CommunityAllocationEntity::getId)
			.map(UUID::toString)
			.orElseThrow(() -> new IllegalStateException("Community allocation not found: " + allocation.id));
	}

	@Override
	public boolean exists(CommunityAllocationId id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(id.id);
	}

	@Override
	public boolean existsByResourceCreditId(ResourceCreditId id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsByResourceCreditId(id.id);
	}

	@Override
	public boolean isUniqueName(String name) {
		return !repository.existsByName(name);
	}

	@Override
	public void delete(CommunityAllocationId id) {
		repository.deleteById(id.id);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
}

