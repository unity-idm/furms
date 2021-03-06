/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credits;


import io.imunity.furms.domain.resource_credits.ResourceCredit;
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
import static org.springframework.util.StringUtils.isEmpty;

@Repository
class ResourceCreditDatabaseRepository implements ResourceCreditRepository {
	private final ResourceCreditEntityRepository repository;

	ResourceCreditDatabaseRepository(ResourceCreditEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<ResourceCredit> findById(String id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(UUID.fromString(id))
			.map(ResourceCreditEntity::toResourceCredit);
	}

	@Override
	public Set<ResourceCredit> findAll(String siteId) {
		return repository.findAllBySiteId(UUID.fromString(siteId))
			.map(ResourceCreditEntity::toResourceCredit)
			.collect(toSet());
	}

	@Override
	public Set<ResourceCredit> findAllNotExpiredByResourceTypeId(String resourceTypeId) {
		return repository.findAllByResourceTypeId(UUID.fromString(resourceTypeId))
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
		return repository.findAllByNameOrSiteName(name)
				.map(ResourceCreditEntity::toResourceCredit)
				.collect(toSet());
	}

	@Override
	public Set<ResourceCredit> findAllNotExpiredByNameOrSiteName(String name) {
		return repository.findAllByNameOrSiteName(name)
				.map(ResourceCreditEntity::toResourceCredit)
				.filter(not(ResourceCredit::isExpired))
				.collect(toSet());
	}

	@Override
	public String create(ResourceCredit credit) {
		ResourceCreditEntity savedResourceCredit = repository.save(
			ResourceCreditEntity.builder()
				.siteId(UUID.fromString(credit.siteId))
				.resourceTypeId(UUID.fromString(credit.resourceTypeId))
				.name(credit.name)
				.split(credit.splittable)
				.amount(credit.amount)
				.createTime(credit.utcCreateTime)
				.startTime(credit.utcStartTime)
				.endTime(credit.utcEndTime)
				.build()
		);
		return savedResourceCredit.getId().toString();
	}

	@Override
	public String update(ResourceCredit credit) {
		return repository.findById(UUID.fromString(credit.id))
			.map(oldResourceCredit -> ResourceCreditEntity.builder()
				.id(oldResourceCredit.getId())
				.siteId(UUID.fromString(credit.siteId))
				.resourceTypeId(UUID.fromString(credit.resourceTypeId))
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
	public boolean exists(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(UUID.fromString(id));
	}

	@Override
	public boolean existsBySiteId(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsBySiteId(UUID.fromString(id));
	}

	@Override
	public boolean existsByResourceTypeId(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsByResourceTypeId(UUID.fromString(id));
	}

	@Override
	public boolean existsByResourceTypeIdIn(Collection<String> ids) {
		if (ids.isEmpty()) {
			return false;
		}
		return repository.existsByResourceTypeIdIn(ids.stream()
			.map(UUID::fromString)
			.collect(toList())
		);
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
