/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credits;


import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Optional.empty;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

@Repository
class ResourceCreditDatabaseRepository implements ResourceCreditRepository {
	private final ResourceCreditEntityRepository repository;

	ResourceCreditDatabaseRepository(ResourceCreditEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<ResourceCredit> findById(ResourceCreditId id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(id.id)
			.map(ResourceCreditEntity::toResourceCredit);
	}

	@Override
	public Set<ResourceCredit> findAll(SiteId siteId) {
		return repository.findAllBySiteId(siteId.id).stream()
			.map(ResourceCreditEntity::toResourceCredit)
			.collect(toSet());
	}

	@Override
	public Set<ResourceCredit> findAllNotExpiredByResourceTypeId(ResourceTypeId resourceTypeId) {
		return repository.findAllByResourceTypeId(resourceTypeId.id).stream()
				.map(ResourceCreditEntity::toResourceCredit)
				.filter(not(ResourceCredit::isExpired))
				.collect(toSet());
	}

	@Override
	public Set<ResourceCredit> findAll() {
		return stream(repository.findAll().spliterator(), false)
			.map(ResourceCreditEntity::toResourceCredit)
			.collect(toSet());
	}

	@Override
	public Set<ResourceCredit> findAllByNameOrSiteName(String name) {
		return repository.findAllByNameOrSiteName(name).stream()
				.map(ResourceCreditEntity::toResourceCredit)
				.collect(toSet());
	}

	@Override
	public Set<ResourceCredit> findAllNotExpiredByNameOrSiteName(String name) {
		return repository.findAllByNameOrSiteName(name).stream()
				.map(ResourceCreditEntity::toResourceCredit)
				.filter(not(ResourceCredit::isExpired))
				.collect(toSet());
	}

	@Override
	public ResourceCreditId create(ResourceCredit resourceCredit) {
		ResourceCreditEntity savedResourceCredit = repository.save(
			ResourceCreditEntity.builder()
				.siteId(resourceCredit.siteId.id)
				.resourceTypeId(resourceCredit.resourceTypeId.id)
				.name(resourceCredit.name)
				.split(resourceCredit.splittable)
				.amount(resourceCredit.amount)
				.createTime(resourceCredit.utcCreateTime)
				.startTime(resourceCredit.utcStartTime)
				.endTime(resourceCredit.utcEndTime)
				.build()
		);
		return new ResourceCreditId(savedResourceCredit.getId());
	}

	@Override
	public void update(ResourceCredit credit) {
		repository.findById(credit.id.id)
			.map(oldResourceCredit -> ResourceCreditEntity.builder()
				.id(oldResourceCredit.getId())
				.siteId(credit.siteId.id)
				.resourceTypeId(credit.resourceTypeId.id)
				.name(credit.name)
				.split(credit.splittable)
				.amount(credit.amount)
				.createTime(credit.utcCreateTime)
				.startTime(credit.utcStartTime)
				.endTime(credit.utcEndTime)
				.build()
			)
			.map(repository::save)
			.map(ResourceCreditEntity::getId)
			.map(UUID::toString)
			.orElseThrow(() -> new IllegalStateException("Credit not found: " + credit));
	}

	@Override
	public boolean exists(ResourceCreditId id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(id.id);
	}

	@Override
	public boolean existsBySiteId(SiteId id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsBySiteId(id.id);
	}

	@Override
	public boolean existsByResourceTypeId(ResourceTypeId id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsByResourceTypeId(id.id);
	}

	@Override
	public boolean existsByResourceTypeIdIn(Collection<ResourceTypeId> ids) {
		if (ids.isEmpty()) {
			return false;
		}
		return repository.existsByResourceTypeIdIn(ids.stream()
			.map(resourceTypeId -> resourceTypeId.id)
			.collect(toList())
		);
	}

	@Override
	public boolean isNamePresent(String name, SiteId siteId) {
		return repository.existsByNameAndSiteId(name, siteId.id);
	}

	@Override
	public void delete(ResourceCreditId id) {
		repository.deleteById(id.id);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	private boolean isEmpty(ResourceTypeId id) {
		return id == null || id.id == null;
	}

	private boolean isEmpty(ResourceCreditId id) {
		return id == null || id.id == null;
	}

	private boolean isEmpty(SiteId id) {
		return id == null || id.id == null;
	}
}
