/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.StringUtils.isEmpty;

@Repository
class SiteDatabaseRepository implements SiteRepository {

	private final SiteEntityRepository repository;

	SiteDatabaseRepository(SiteEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Site> findById(String id) {
		if (isEmpty(id)) {
			return Optional.empty();
		}
		return repository.findById(fromString(id))
				.map(SiteEntity::toSite);
	}

	@Override
	public Set<Site> findAll() {
		return stream(repository.findAll().spliterator(), false)
				.map(SiteEntity::toSite)
				.collect(toSet());
	}

	@Override
	public String create(Site site) {
		validateSiteName(site);
		SiteEntity saved = repository.save(SiteEntity.builder()
				.name(site.getName())
				.build());
		return saved.getId().toString();
	}

	@Override
	public String update(Site site) {
		validateSiteId(site);
		validateSiteName(site);

		return repository.findById(fromString(site.getId()))
				.map(oldEntity -> SiteEntity.builder()
						.id(oldEntity.getId())
						.name(site.getName())
						.build())
				.map(repository::save)
				.map(SiteEntity::getId)
				.map(UUID::toString)
				.get();
	}

	@Override
	public boolean exists(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(fromString(id));
	}

	@Override
	public boolean isUniqueName(String name) {
		return !repository.existsByName(name);
	}

	@Override
	public void delete(String id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Incorrect delete Site input.");
		}
		repository.deleteById(fromString(id));
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
	
	private void validateSiteName(final Site site) {
		if (site == null || isEmpty(site.getName()) || !isUniqueName(site.getName())) {
			throw new IllegalArgumentException("Incorrect Site name input.");
		}
	}

	private void validateSiteId(final Site site) {
		if (site == null || isEmpty(site.getId()) || !repository.existsById(fromString(site.getId()))) {
			throw new IllegalArgumentException("Incorrect Site name input.");
		}
	}
}
