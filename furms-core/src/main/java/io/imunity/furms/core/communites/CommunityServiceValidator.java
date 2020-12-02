/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.springframework.stereotype.Component;

import static org.springframework.util.Assert.notNull;

@Component
public class CommunityServiceValidator {
	private final CommunityRepository communityRepository;

	public CommunityServiceValidator(CommunityRepository communityRepository) {
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
	}

	void validateDelete(String id) {
		validateId(id);
	}

	private void validateName(Community community) {
		notNull(community.getUserFacingName(), "Community user facing name has to be declared.");
		if (!communityRepository.isUniqueUserFacingName(community.getUserFacingName())) {
			throw new IllegalArgumentException("Community user facing  name has to be unique.");
		}
	}

	private void validateId(String id) {
		notNull(id, "Community ID has to be declared.");
		if (!communityRepository.exists(id)) {
			throw new IllegalArgumentException("Community with declared ID is not exists.");
		}
	}
}
