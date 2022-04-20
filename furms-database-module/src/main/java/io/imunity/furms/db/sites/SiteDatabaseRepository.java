/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.ObjectUtils.isEmpty;

@Repository
class SiteDatabaseRepository implements SiteRepository {

	private final SiteEntityRepository repository;

	SiteDatabaseRepository(SiteEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Site> findById(SiteId id) {
		if (isEmpty(id)) {
			return Optional.empty();
		}
		return repository.findById(id.id)
				.map(SiteEntity::toSite);
	}

	@Override
	public Set<Site> findAll(Set<SiteId> ids) {
		return repository.findAllByIdIn(ids.stream().map(siteId -> siteId.id).collect(toSet())).stream()
			.map(SiteEntity::toSite)
			.collect(toSet());
	}

	@Override
	public SiteExternalId findByIdExternalId(SiteId id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Id should not be null");
		}
		return repository.findExternalId(id.id)
			.map(SiteExternalId::new)
			.orElseThrow(() -> new IllegalArgumentException("External Id doesn't exist"));
	}

	@Override
	public SiteId findByExternalId(SiteExternalId externalId) {
		if (isEmpty(externalId)) {
			throw new IllegalArgumentException("Id should not be null");
		}
		return repository.findId(externalId.id)
			.map(id -> new SiteId(id, externalId))
			.orElseThrow(() -> new IllegalArgumentException("External Id doesn't exist"));
	}

	@Override
	public Set<SiteId> findByProjectId(ProjectId id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Id should not be null");
		}
		return repository.findRelatedSites(id.id).stream()
			.map(site -> new SiteId(site.getId().toString(), new SiteExternalId(site.getExternalId())))
			.collect(toSet());
	}

	@Override
	public Map<String, Set<String>> findRelatedProjectIds(SiteId siteId) {
		if (isEmpty(siteId)) {
			throw new IllegalArgumentException("Id should not be null");
		}
		return repository.findRelatedProjectIds(siteId.id).stream()
			.collect(groupingBy(x -> x.communityId.toString(), mapping(x -> x.projectId.toString(), toSet())));
	}

	@Override
	public Set<Site> findAll() {
		return stream(repository.findAll().spliterator(), false)
				.map(SiteEntity::toSite)
				.collect(toSet());
	}

	@Override
	public String create(Site site, SiteExternalId siteExternalId) {
		validateSiteName(site);
		SiteEntity saved = repository.save(SiteEntity.builder()
				.name(site.getName())
				.connectionInfo(site.getConnectionInfo())
				.logo(site.getLogo())
				.sshKeyFromOptionMandatory(site.isSshKeyFromOptionMandatory())
				.externalId(siteExternalId.id)
				.policyId(site.getPolicyId().id)
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
						.oauthClientId(site.getOauthClientId())
						.connectionInfo(site.getConnectionInfo())
						.logo(site.getLogo())
						.sshKeyFromOptionMandatory(site.isSshKeyFromOptionMandatory())
						.sshKeyHistoryLength(site.getSshKeyHistoryLength())
						.externalId(oldEntity.getExternalId())
						.policyId(site.getPolicyId().id)
						.build())
				.map(repository::save)
				.map(SiteEntity::getId)
				.map(UUID::toString)
				.orElseThrow(() -> new IllegalStateException("Site not found: " + site));
	}

	@Override
	public boolean exists(SiteId id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(id.id);
	}

	@Override
	public boolean existsByExternalId(SiteExternalId siteExternalId) {
		if (isEmpty(siteExternalId.id)) {
			throw new IllegalArgumentException("External id should not be null");
		}
		return repository.existsByExternalId(siteExternalId.id);
	}

	@Override
	public boolean isNamePresent(String name) {
		return repository.existsByName(name);
	}

	@Override
	public boolean isNamePresentIgnoringRecord(String name, String recordToIgnore) {
		return repository.existsByNameAndIdIsNot(name, fromString(recordToIgnore));
	}

	@Override
	public void delete(SiteId id) {
		assertTrue(!isEmpty(id), () -> new IllegalArgumentException("Incorrect delete Site input: ID is empty"));

		repository.deleteById(id.id);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
	
	private void validateSiteName(final Site site) {
		assertTrue(site != null, () -> new IllegalArgumentException("Site object is missing."));
		assertTrue(!isEmpty(site.getName()), () -> new IllegalArgumentException("Incorrect Site name: name is empty"));
	}

	private void validateSiteId(final Site site) {
		assertTrue(site != null, () -> new IllegalArgumentException("Site object is missing."));
		assertTrue(!isEmpty(site.getId()), () -> new IllegalArgumentException("Incorrect Site ID: ID is empty."));
		assertTrue(repository.existsById(fromString(site.getId())), () -> new IllegalArgumentException("Incorrect Site ID: ID not exists in DB."));
	}
}
