/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static org.springframework.util.Assert.notNull;

@Component
class CommunityServiceValidator {
	private final CommunityRepository communityRepository;

	CommunityServiceValidator(CommunityRepository communityRepository) {
		this.communityRepository = communityRepository;
	}

	void validateCreate(Community community) {
		notNull(community, "Community object cannot be null.");
		validateName(community);
	}

	void validateUpdate(Community request) {
		notNull(request, "Community object cannot be null.");
		validateId(request.getId());
		validateName(request);
		validateDescription(request);
	}

	void validateDelete(String id) {
		validateId(id);
	}

	private void validateName(Community community) {
		notNull(community.getName(), "Community name has to be declared.");
		if (isNameUnique(community)) {
			throw new IllegalArgumentException("Community name has to be unique.");
		}
		if (community.getName().length() > 255) {
			throw new IllegalArgumentException("Community name is too long.");
		}
	}

	private void validateDescription(Community community) {
		if (Objects.nonNull(community.getDescription()) && community.getDescription().length() > 510) {
			throw new IllegalArgumentException("Community description is too long.");
		}
	}

	private boolean isNameUnique(Community community) {
		Optional<Community> optionalCommunity = communityRepository.findById(community.getId());
		return !communityRepository.isUniqueName(community.getName()) &&
			(optionalCommunity.isEmpty() || !optionalCommunity.get().getName().equals(community.getName()));
	}

	private void validateId(String id) {
		notNull(id, "Community ID has to be declared.");
		if (!communityRepository.exists(id)) {
			throw new IllegalArgumentException("Community with declared ID is not exists.");
		}
	}
}
