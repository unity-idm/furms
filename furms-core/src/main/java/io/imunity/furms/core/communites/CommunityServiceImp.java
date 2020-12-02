/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
class CommunityServiceImp implements CommunityService {
	private final CommunityRepository communityRepository;
	private final CommunityServiceValidator validator;

	CommunityServiceImp(CommunityRepository communityRepository,
	                    CommunityServiceValidator validator) {
		this.communityRepository = communityRepository;
		this.validator = validator;
	}

	@Override
	public Optional<Community> findById(String id) {
		return communityRepository.findById(id);
	}

	@Override
	public Set<Community> findAll() {
		return communityRepository.findAll();
	}

	@Override
	public void create(Community community) {
		validator.validateCreate(community);

		communityRepository.save(community);
	}

	@Override
	public void update(Community community) {
		validator.validateUpdate(community);

		communityRepository.save(community);
	}

	@Override
	public void delete(String id) {
		validator.validateDelete(id);

		communityRepository.delete(id);
	}
}
