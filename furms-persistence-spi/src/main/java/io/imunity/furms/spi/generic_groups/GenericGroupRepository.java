/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.generic_groups;

import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignment;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignmentId;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignmentAmount;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignments;
import io.imunity.furms.domain.generic_groups.GroupAccess;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Optional;
import java.util.Set;

public interface GenericGroupRepository {
	Optional<GenericGroup> findBy(GenericGroupId genericGroupId);
	Optional<GenericGroupWithAssignments> findGroupWithAssignments(String communityId, GenericGroupId genericGroupId);

	Set<GenericGroupWithAssignmentAmount> findAllGroupWithAssignmentsAmount(String communityId);

	Set<GenericGroup> findAllBy(String communityId);
	Set<GenericGroupAssignment> findAllBy(GenericGroupId id);
	Set<GroupAccess> findAllBy(FenixUserId id);

	GenericGroupId create(GenericGroup group);
	GenericGroupAssignmentId create(GenericGroupAssignment assignment);

	void update(GenericGroup group);
	void delete(GenericGroupId id);
	void delete(GenericGroupAssignmentId id);

	boolean existsBy(String communityId, GenericGroupId groupId);
	boolean existsBy(String communityId, GenericGroupAssignmentId assignmentId);
	boolean existsBy(GenericGroupId groupId, FenixUserId userId);
	boolean existsBy(String communityId, String name);
}
