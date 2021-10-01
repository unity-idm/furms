/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.generic_groups;

import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignmentWithUser;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignmentAmount;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignments;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Optional;
import java.util.Set;

public interface GenericGroupService {
	Optional<GenericGroup> findBy(String communityId, GenericGroupId id);
	Optional<GenericGroupWithAssignments> findGroupWithAssignments(String communityId, GenericGroupId id);
	Set<GenericGroup> findAll(String communityId);
	Set<GenericGroupWithAssignmentAmount> findAllGroupWithAssignmentsAmount(String communityId);
	Set<GenericGroupAssignmentWithUser> findAll(String communityId, GenericGroupId id);
	GenericGroupId create(GenericGroup group);
	void createMembership(String communityId, GenericGroupId groupId, FenixUserId userId);
	void update(GenericGroup group);
	void delete(String communityId, GenericGroupId id);
	void deleteMembership(String communityId, GenericGroupId groupId, FenixUserId fenixUserId);
}
