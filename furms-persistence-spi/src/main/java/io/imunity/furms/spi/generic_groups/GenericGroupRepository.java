/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.generic_groups;

import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupMembership;
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
	Set<GenericGroupMembership> findAllBy(GenericGroupId id);
	Set<GroupAccess> findAllBy(FenixUserId id);

	GenericGroupId create(GenericGroup group);
	void createMembership(GenericGroupMembership assignment);

	void update(GenericGroup group);
	void delete(GenericGroupId id);
	void deleteMembership(GenericGroupId groupId, FenixUserId userId);

	boolean existsBy(String communityId, GenericGroupId groupId);
	boolean existsBy(GenericGroupId groupId, FenixUserId userId);
	boolean existsBy(String communityId, String name);
}
