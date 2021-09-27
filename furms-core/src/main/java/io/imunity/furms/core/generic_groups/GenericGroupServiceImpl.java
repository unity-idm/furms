/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.generic_groups;

import io.imunity.furms.api.generic_groups.GenericGroupService;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.GroupNotBelongToCommunityError;
import io.imunity.furms.api.validation.exceptions.UserAlreadyIsInGroupError;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignment;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignmentId;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignmentWithUser;
import io.imunity.furms.domain.generic_groups.GenericGroupCreateEvent;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.generic_groups.GenericGroupRemoveEvent;
import io.imunity.furms.domain.generic_groups.GenericGroupUpdateEvent;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignmentAmount;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignments;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.generic_groups.GenericGroupRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static java.util.stream.Collectors.toMap;

@Service
class GenericGroupServiceImpl implements GenericGroupService {
	private final GenericGroupRepository genericGroupRepository;
	private final UsersDAO usersDAO;
	private final ApplicationEventPublisher publisher;

	GenericGroupServiceImpl(GenericGroupRepository genericGroupRepository, UsersDAO usersDAO, ApplicationEventPublisher publisher) {
		this.genericGroupRepository = genericGroupRepository;
		this.usersDAO = usersDAO;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<GenericGroup> findAll(String communityId) {
		return genericGroupRepository.findAllBy(communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Optional<GenericGroupWithAssignments> findGroupWithAssignments(String communityId, GenericGroupId genericGroupId) {
		return genericGroupRepository.findGroupWithAssignments(communityId, genericGroupId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Optional<GenericGroup> findBy(String communityId, GenericGroupId genericGroupId) {
		validCommunityAndGenericGroupBelongs(communityId, genericGroupId);
		return genericGroupRepository.findBy(genericGroupId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<GenericGroupWithAssignmentAmount> findAllGroupWithAssignmentsAmount(String communityId) {
		return genericGroupRepository.findAllGroupWithAssignmentsAmount(communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<GenericGroupAssignmentWithUser> findAll(String communityId, GenericGroupId id) {
		validCommunityAndGenericGroupBelongs(communityId, id);
		Map<FenixUserId, FURMSUser> collect = usersDAO.getAllUsers().stream()
			.filter(x -> x.fenixUserId.isPresent())
			.collect(toMap(x -> x.fenixUserId.get(), Function.identity()));
		return genericGroupRepository.findAllBy(id).stream()
			.map(x -> new GenericGroupAssignmentWithUser(collect.get(x.fenixUserId), x))
			.collect(Collectors.toSet());
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "group.communityId")
	public GenericGroupId create(GenericGroup group) {
		validNotNull(group);
		validIsUnique(group.communityId, group.name);
		GenericGroupId genericGroupId = genericGroupRepository.create(group);
		publisher.publishEvent(new GenericGroupCreateEvent(genericGroupId));
		return genericGroupId;
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public GenericGroupAssignmentId create(String communityId, GenericGroupAssignment assignment) {
		validNotNull(assignment);
		validIsUnique(assignment.genericGroupId, assignment.fenixUserId);
		validCommunityAndGenericGroupBelongs(communityId, assignment.genericGroupId);
		return genericGroupRepository.create(assignment);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "group.communityId")
	public void update(GenericGroup group) {
		validNotNull(group);
		validIsUnique(group.id, group.communityId, group.name);
		genericGroupRepository.update(group);
		publisher.publishEvent(new GenericGroupUpdateEvent(group.id));
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void delete(String communityId, GenericGroupId id) {
		validCommunityAndGenericGroupBelongs(communityId, id);
		genericGroupRepository.delete(id);
		publisher.publishEvent(new GenericGroupRemoveEvent(id));
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void delete(String communityId, GenericGroupAssignmentId id) {
		validCommunityAndGenericGroupBelongs(communityId, id);
		genericGroupRepository.delete(id);
	}

	private void validCommunityAndGenericGroupBelongs(String communityId, GenericGroupId id) {
		if(!genericGroupRepository.existsBy(communityId, id))
			throw new GroupNotBelongToCommunityError(String.format("Group %s doesn't belong to community %s", id.id, communityId));
	}

	private void validCommunityAndGenericGroupBelongs(String communityId, GenericGroupAssignmentId id) {
		if(!genericGroupRepository.existsBy(communityId, id))
			throw new GroupNotBelongToCommunityError(String.format("Group %s doesn't belong to community %s", id.id, communityId));
	}

	private void validIsUnique(GenericGroupId id, FenixUserId fenixUserId) {
		if(genericGroupRepository.existsBy(id, fenixUserId))
			throw new UserAlreadyIsInGroupError(String.format("User %s is already in group %s", fenixUserId.id, id.id));
	}

	private void validIsUnique(String communityId, String name) {
		if(genericGroupRepository.existsBy(communityId, name))
			throw new DuplicatedNameValidationError(String.format("Group name: %s - already exists", name));
	}

	private void validNotNull(GenericGroupAssignment assignment) {
		Assert.notNull(assignment, "GroupAssignment object cannot be null.");
		Assert.notNull(assignment.fenixUserId, "FenixUserId cannot be null.");
		Assert.notNull(assignment.genericGroupId, "GenericGroupId cannot be null.");
		Assert.notNull(assignment.utcMemberSince, "MemberSince cannot be null.");
	}

	private void validNotNull(GenericGroup group) {
		Assert.notNull(group, "Group object cannot be null.");
		Assert.notNull(group.communityId, "CommunityId cannot be null.");
		Assert.notNull(group.name, "Name cannot be null.");
	}

	private void validIsUnique(GenericGroupId groupId, String communityId, String name) {
		boolean present = genericGroupRepository.findBy(groupId)
			.filter(x -> !x.name.equals(name))
			.isPresent();
		if(present && genericGroupRepository.existsBy(communityId, name))
			throw new DuplicatedNameValidationError(String.format("Group name: %s - already exists", name));
	}
}
