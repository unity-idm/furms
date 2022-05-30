/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.generic_groups;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignmentWithUser;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignmentAmount;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignments;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Optional;
import java.util.Set;

public interface GenericGroupService {
	Optional<GenericGroup> findBy(CommunityId communityId, GenericGroupId id);
	Optional<GenericGroupWithAssignments> findGroupWithAssignments(CommunityId communityId, GenericGroupId id);
	Set<GenericGroup> findAll(CommunityId communityId);
	Set<GenericGroupWithAssignmentAmount> findAllGroupWithAssignmentsAmount(CommunityId communityId);
	Set<GenericGroupAssignmentWithUser> findAll(CommunityId communityId, GenericGroupId id);
	GenericGroupId create(GenericGroup group);
	void createMembership(CommunityId communityId, GenericGroupId groupId, FenixUserId userId);
	void update(GenericGroup group);
	void delete(CommunityId communityId, GenericGroupId id);
	void deleteMembership(CommunityId communityId, GenericGroupId groupId, FenixUserId fenixUserId);
}
