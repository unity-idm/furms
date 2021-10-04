/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.generic_groups;

import io.imunity.furms.api.generic_groups.GenericGroupService;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.GroupNotBelongingToCommunityException;
import io.imunity.furms.api.validation.exceptions.UserAlreadyIsInGroupError;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignmentWithUser;
import io.imunity.furms.domain.generic_groups.GenericGroupCreateEvent;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.generic_groups.GenericGroupMembership;
import io.imunity.furms.domain.generic_groups.GenericGroupRemoveEvent;
import io.imunity.furms.domain.generic_groups.GenericGroupUpdateEvent;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignmentAmount;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignments;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.generic_groups.GenericGroupRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.Capability.MEMBERSHIP_GROUP_READ;
import static io.imunity.furms.domain.authz.roles.Capability.MEMBERSHIP_GROUP_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.Assert.notNull;

@Service
class GenericGroupServiceImpl implements GenericGroupService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final GenericGroupRepository genericGroupRepository;
	private final UsersDAO usersDAO;
	private final Clock clock;
	private final ApplicationEventPublisher publisher;

	GenericGroupServiceImpl(GenericGroupRepository genericGroupRepository, UsersDAO usersDAO, Clock clock, ApplicationEventPublisher publisher) {
		this.genericGroupRepository = genericGroupRepository;
		this.usersDAO = usersDAO;
		this.clock = clock;
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
		assertGroupBelongsToCommunity(communityId, genericGroupId);
		return genericGroupRepository.findBy(genericGroupId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<GenericGroupWithAssignmentAmount> findAllGroupWithAssignmentsAmount(String communityId) {
		return genericGroupRepository.findAllGroupWithAssignmentsAmount(communityId);
	}

	@Override
	@FurmsAuthorize(capability = MEMBERSHIP_GROUP_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<GenericGroupAssignmentWithUser> findAll(String communityId, GenericGroupId id) {
		assertGroupBelongsToCommunity(communityId, id);
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
		assertNotNull(group);
		assertUniqueness(group.communityId, group.name);
		GenericGroupId genericGroupId = genericGroupRepository.create(group);
		LOG.info("Generic group with given ID: {} was created: {}", genericGroupId.id, group);
		publisher.publishEvent(new GenericGroupCreateEvent(genericGroupId));
		return genericGroupId;
	}

	@Override
	@FurmsAuthorize(capability = MEMBERSHIP_GROUP_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void createMembership(String communityId, GenericGroupId groupId, FenixUserId userId) {
		assertNotNull(communityId, groupId, userId);
		assertUniqueness(groupId, userId);
		assertGroupBelongsToCommunity(communityId, groupId);
		genericGroupRepository.createMembership(
			GenericGroupMembership.builder()
				.genericGroupId(groupId)
				.fenixUserId(userId)
				.utcMemberSince(convertToUTCTime(ZonedDateTime.now(clock)))
				.build()
		);
		LOG.info("Membership in group ID: {} for user ID: {} was created", groupId.id, userId.id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "group.communityId")
	public void update(GenericGroup group) {
		assertNotNull(group);
		assertUniqueness(group.id, group.communityId, group.name);
		genericGroupRepository.update(group);
		LOG.info("Generic group with given ID: {} was updated: {}", group.id.id, group);
		publisher.publishEvent(new GenericGroupUpdateEvent(group.id));
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void delete(String communityId, GenericGroupId id) {
		assertGroupBelongsToCommunity(communityId, id);
		genericGroupRepository.delete(id);
		LOG.info("Generic group with given ID: {} was removed", id.id);
		publisher.publishEvent(new GenericGroupRemoveEvent(id));
	}

	@Override
	@FurmsAuthorize(capability = MEMBERSHIP_GROUP_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void deleteMembership(String communityId,  GenericGroupId groupId, FenixUserId fenixUserId) {
		assertNotNull(communityId, groupId, fenixUserId);
		assertGroupBelongsToCommunity(communityId, groupId);
		genericGroupRepository.deleteMembership(groupId, fenixUserId);
		LOG.info("Membership in group ID: {} for user ID: {} was removed", groupId.id, fenixUserId.id);
	}

	private void assertGroupBelongsToCommunity(String communityId, GenericGroupId id) {
		if(!genericGroupRepository.existsBy(communityId, id))
			throw new GroupNotBelongingToCommunityException(String.format("Group %s doesn't belong to community %s", id.id, communityId));
	}

	private void assertUniqueness(GenericGroupId id, FenixUserId fenixUserId) {
		if(genericGroupRepository.existsBy(id, fenixUserId))
			throw new UserAlreadyIsInGroupError(String.format("User %s is already in group %s", fenixUserId.id, id.id));
	}

	private void assertUniqueness(String communityId, String name) {
		if(genericGroupRepository.existsBy(communityId, name))
			throw new DuplicatedNameValidationError(String.format("Group name: %s - already exists", name));
	}

	private void assertNotNull(GenericGroup group) {
		notNull(group, "Group object cannot be null.");
		notNull(group.communityId, "CommunityId cannot be null.");
		notNull(group.name, "Name cannot be null.");
	}

	private void assertNotNull(String communityId, GenericGroupId groupId, FenixUserId userId) {
		notNull(groupId, "Group object cannot be null.");
		notNull(groupId.id, "Group object cannot be null.");
		notNull(communityId, "CommunityId cannot be null.");
		notNull(userId, "Fenix user id cannot be null.");
		notNull(userId.id, "Fenix user id cannot be null.");
	}

	private void assertUniqueness(GenericGroupId groupId, String communityId, String name) {
		boolean present = genericGroupRepository.findBy(groupId)
			.filter(x -> !x.name.equals(name))
			.isPresent();
		if(present && genericGroupRepository.existsBy(communityId, name))
			throw new DuplicatedNameValidationError(String.format("Group name: %s - already exists", name));
	}
}