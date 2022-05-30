/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.api.validation.exceptions.RemovingCommunityException;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static io.imunity.furms.core.constant.ValidationConst.MAX_DESCRIPTION_LENGTH;
import static io.imunity.furms.core.constant.ValidationConst.MAX_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static org.springframework.util.Assert.notNull;

@Component
class CommunityServiceValidator {
	private final CommunityRepository communityRepository;
	private final ProjectRepository projectRepository;

	CommunityServiceValidator(CommunityRepository communityRepository, ProjectRepository projectRepository) {
		this.communityRepository = communityRepository;
		this.projectRepository = projectRepository;
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

	void validateDelete(CommunityId id) {
		validateId(id);
		if(!projectRepository.findAllByCommunityId(id).isEmpty())
			throw new RemovingCommunityException("Removed Community cannot have projects");
	}

	private void validateName(Community community) {
		notNull(community.getName(), "Community name has to be declared.");
		if (isNameUnique(community)) {
			throw new DuplicatedNameValidationError("Community name has to be unique.");
		}
		if (community.getName().length() > MAX_NAME_LENGTH) {
			throw new IllegalArgumentException("Community name is too long.");
		}
	}

	private void validateDescription(Community community) {
		if (Objects.nonNull(community.getDescription()) && community.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
			throw new IllegalArgumentException("Community description is too long.");
		}
	}

	private boolean isNameUnique(Community community) {
		Optional<Community> optionalCommunity = communityRepository.findById(community.getId());
		return !communityRepository.isUniqueName(community.getName()) &&
			(optionalCommunity.isEmpty() || !optionalCommunity.get().getName().equals(community.getName()));
	}

	private void validateId(CommunityId id) {
		notNull(id, "Community ID has to be declared.");
		assertTrue(communityRepository.exists(id), () -> new IdNotFoundValidationError("Community with declared ID is not exists."));

	}
}
