/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.generic_groups;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.generic_groups.GenericGroupMembership;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignmentAmount;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignments;
import io.imunity.furms.domain.generic_groups.GroupAccess;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.generic_groups.GenericGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

@Repository
class GenericGroupDatabaseRepository implements GenericGroupRepository {
	private final GenericGroupEntityRepository genericGroupEntityRepository;
	private final GenericGroupMembershipEntityRepository genericGroupMembershipEntityRepository;

	GenericGroupDatabaseRepository(GenericGroupEntityRepository genericGroupEntityRepository,
	                               GenericGroupMembershipEntityRepository genericGroupMembershipEntityRepository) {
		this.genericGroupEntityRepository = genericGroupEntityRepository;
		this.genericGroupMembershipEntityRepository = genericGroupMembershipEntityRepository;
	}

	@Override
	public Optional<GenericGroup> findBy(GenericGroupId genericGroupId) {
		return genericGroupEntityRepository.findById(genericGroupId.id)
			.map(genericGroup -> GenericGroup.builder()
				.id(genericGroup.getId())
				.communityId(genericGroup.communityId)
				.name(genericGroup.name)
				.description(genericGroup.description)
				.build()
			);
	}

	@Override
	public Optional<GenericGroupWithAssignments> findGroupWithAssignments(CommunityId communityId, GenericGroupId genericGroupId) {
		return genericGroupEntityRepository.findAllAssignments(communityId.id, genericGroupId.id).stream()
				.filter(Objects::nonNull)
				.collect(groupingBy(
					groupWithAssignments -> GenericGroup.builder()
							.id(groupWithAssignments.getId())
							.communityId(groupWithAssignments.communityId)
							.name(groupWithAssignments.name)
							.description(groupWithAssignments.description)
							.build(),
					mapping(
						groupWithAssignments ->
							groupWithAssignments.userId == null
							? null
							: GenericGroupMembership.builder()
											.genericGroupId(groupWithAssignments.getId())
											.fenixUserId(groupWithAssignments.userId)
											.utcMemberSince(groupWithAssignments.memberSince)
											.build(),
							toSet()
					))
				).entrySet().stream()
				.map(x -> new GenericGroupWithAssignments(
						x.getKey(),
						x.getValue().stream()
								.filter(Objects::nonNull)
								.collect(toSet())))
				.findAny();
	}

	@Override
	public Set<GenericGroupWithAssignmentAmount> findAllGroupWithAssignmentsAmount(CommunityId communityId) {
		return genericGroupEntityRepository.findAllWithAssignmentAmount(communityId.id).stream()
			.map(entity -> new GenericGroupWithAssignmentAmount(
				GenericGroup.builder()
					.id(entity.getId())
					.communityId(entity.communityId)
					.name(entity.name)
					.description(entity.description)
					.build(),
				entity.membershipAmount
			)).collect(Collectors.toSet());
	}

	@Override
	public Set<GenericGroup> findAllBy(CommunityId communityId) {
		return genericGroupEntityRepository.findAllByCommunityId(communityId.id).stream()
			.map(entity -> GenericGroup.builder()
				.id(entity.getId())
				.communityId(entity.communityId)
				.name(entity.name)
				.description(entity.description)
				.build()
			).collect(toSet());
	}

	@Override
	public Set<GenericGroupMembership> findAllBy(GenericGroupId id) {
		return genericGroupMembershipEntityRepository.findAllByGenericGroupId(id.id).stream()
			.map(entity -> GenericGroupMembership.builder()
				.genericGroupId(entity.genericGroupId)
				.fenixUserId(entity.userId)
				.utcMemberSince(entity.memberSince)
				.build()
			).collect(toSet());
	}

	@Override
	public Set<GroupAccess> findAllBy(FenixUserId userId) {
		return genericGroupEntityRepository.findAllAssignments(userId.id).stream()
			.collect(groupingBy(x -> x.communityId, mapping(x -> x.name, toSet())))
			.entrySet().stream()
			.map(entry -> new GroupAccess(entry.getKey().toString(), entry.getValue()))
			.collect(toSet());
	}

	@Override
	public GenericGroupId create(GenericGroup group) {
		GenericGroupEntity genericGroupEntity = genericGroupEntityRepository.save(
			GenericGroupEntity.builder()
				.communityId(group.communityId.id)
				.name(group.name)
				.description(group.description)
				.build()
		);
		return new GenericGroupId(genericGroupEntity.getId());
	}

	@Override
	public void createMembership(GenericGroupMembership groupMembership) {
		genericGroupMembershipEntityRepository.save(
			GenericGroupMembershipEntity.builder()
				.genericGroupId(groupMembership.genericGroupId.id)
				.userId(groupMembership.fenixUserId.id)
				.memberSince(groupMembership.utcMemberSince)
				.build()
		);
	}

	@Override
	public void update(GenericGroup group) {
		genericGroupEntityRepository.findById(group.id.id).ifPresent(groupEntity -> genericGroupEntityRepository.save(
			GenericGroupEntity.builder()
				.id(groupEntity.getId())
				.communityId(groupEntity.communityId)
				.name(group.name)
				.description(group.description)
				.build()
		));
	}

	@Override
	public void delete(GenericGroupId id) {
		genericGroupEntityRepository.deleteById(id.id);
	}

	@Override
	public void deleteMembership(GenericGroupId id, FenixUserId userId) {
		genericGroupMembershipEntityRepository.deleteByGenericGroupIdAndUserId(id.id, userId.id);
	}

	@Override
	public boolean existsBy(CommunityId communityId, GenericGroupId groupId) {
		return genericGroupEntityRepository.existsByCommunityIdAndId(communityId.id, groupId.id);
	}

	@Override
	public boolean existsBy(GenericGroupId groupId, FenixUserId userId) {
		return genericGroupMembershipEntityRepository.existsByGenericGroupIdAndUserId(groupId.id, userId.id);
	}

	@Override
	public boolean existsBy(CommunityId communityId, String name) {
		return genericGroupEntityRepository.existsByCommunityIdAndName(communityId.id, name);
	}
}

