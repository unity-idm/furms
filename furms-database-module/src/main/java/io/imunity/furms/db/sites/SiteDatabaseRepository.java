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

import static io.imunity.furms.db.sites.SiteEntityUtils.generateSiteId;
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
		return repository.findBySiteId(id)
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
				.siteId(generateSiteId())
				.name(site.getName())
				.build());
		return saved.getSiteId();
	}

	@Override
	public String update(Site site) {
		validateSiteId(site);
		validateSiteName(site);

		return repository.findBySiteId(site.getId())
				.map(oldEntity -> SiteEntity.builder()
						.id(oldEntity.getId())
						.siteId(oldEntity.getSiteId())
						.name(site.getName())
						.build())
				.map(repository::save)
				.map(SiteEntity::getName)
				.get();
	}

	@Override
	public boolean exists(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsBySiteId(id);
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
		repository.deleteBySiteId(id);
	}

	private void validateSiteName(final Site site) {
		if (site == null || isEmpty(site.getName()) || !isUniqueName(site.getName())) {
			throw new IllegalArgumentException("Incorrect Site name input.");
		}
	}

	private void validateSiteId(final Site site) {
		if (site == null || isEmpty(site.getId()) || !repository.existsBySiteId(site.getId())) {
			throw new IllegalArgumentException("Incorrect Site name input.");
		}
	}
}
