/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.communities;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.ObjectUtils.isEmpty;

@Repository
class CommunityDatabaseRepository implements CommunityRepository {

	private final CommunityEntityRepository repository;

	CommunityDatabaseRepository(CommunityEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Community> findById(String id) {
		if (isEmpty(id)) {
			return Optional.empty();
		}
		return repository.findById(fromString(id))
				.map(CommunityEntity::toCommunity);
	}

	@Override
	public Set<Community> findAll() {
		return stream(repository.findAll().spliterator(), false)
				.map(CommunityEntity::toCommunity)
				.collect(toSet());
	}

	@Override
	public String create(Community community) {
		CommunityEntity saved = repository.save(CommunityEntity.builder()
				.name(community.getName())
				.description(community.getDescription())
				.logo(community.getLogo().getImage(), community.getLogo().getType())
				.build());
		return saved.getId().toString();
	}

	@Override
	public void update(Community community) {
		repository.findById(fromString(community.getId()))
			.map(oldEntity -> CommunityEntity.builder()
				.id(oldEntity.getId())
				.name(community.getName())
				.description(community.getDescription())
				.logo(community.getLogo().getImage(), community.getLogo().getType())
				.build())
			.map(repository::save)
			.map(CommunityEntity::getId)
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
			throw new IllegalArgumentException("Incorrect delete Community input.");
		}
		repository.deleteById(fromString(id));
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
}
