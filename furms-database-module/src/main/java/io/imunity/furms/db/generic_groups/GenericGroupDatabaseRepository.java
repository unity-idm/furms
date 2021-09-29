/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.generic_groups;

import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignment;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignmentId;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignmentAmount;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignments;
import io.imunity.furms.domain.generic_groups.GroupAccess;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.generic_groups.GenericGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

@Repository
class GenericGroupDatabaseRepository implements GenericGroupRepository {
	private final GenericGroupEntityRepository genericGroupEntityRepository;
	private final GenericGroupAssignmentEntityRepository genericGroupAssignmentEntityRepository;

	GenericGroupDatabaseRepository(GenericGroupEntityRepository genericGroupEntityRepository,
	                               GenericGroupAssignmentEntityRepository genericGroupAssignmentEntityRepository) {
		this.genericGroupEntityRepository = genericGroupEntityRepository;
		this.genericGroupAssignmentEntityRepository = genericGroupAssignmentEntityRepository;
	}

	@Override
	public Optional<GenericGroup> findBy(GenericGroupId genericGroupId) {
		return genericGroupEntityRepository.findById(genericGroupId.id)
			.map(x -> GenericGroup.builder()
				.id(x.getId())
				.communityId(x.communityId.toString())
				.name(x.name)
				.description(x.description)
				.build()
			);
	}

	@Override
	public Optional<GenericGroupWithAssignments> findGroupWithAssignments(String communityId, GenericGroupId genericGroupId) {
		return genericGroupEntityRepository.findAllAssignments(UUID.fromString(communityId), genericGroupId.id).stream()
			.collect(groupingBy(
				x ->
					GenericGroup.builder()
						.id(x.getId())
						.communityId(x.communityId.toString())
						.name(x.name)
						.description(x.description)
						.build(),
				mapping(
					x ->
						GenericGroupAssignment.builder()
							.id(x.assignmentId)
							.genericGroupId(x.getId())
							.fenixUserId(x.userId)
							.utcMemberSince(x.memberSince)
							.build(),
					toSet()
				))
			).entrySet().stream()
			.map(x -> new GenericGroupWithAssignments(x.getKey(), x.getValue()))
			.findAny();
	}

	@Override
	public Set<GenericGroupWithAssignmentAmount> findAllGroupWithAssignmentsAmount(String communityId) {
		return genericGroupEntityRepository.findAllWithAssignmentAmount(UUID.fromString(communityId)).stream()
			.map(x -> new GenericGroupWithAssignmentAmount(
				GenericGroup.builder()
					.id(x.getId())
					.communityId(x.communityId.toString())
					.name(x.name)
					.description(x.description)
					.build(),
				x.assignmentAmount
			)).collect(Collectors.toSet());
	}

	@Override
	public Set<GenericGroup> findAllBy(String communityId) {
		return genericGroupEntityRepository.findAllByCommunityId(UUID.fromString(communityId)).stream()
			.map(entity -> GenericGroup.builder()
				.id(entity.getId())
				.communityId(entity.communityId.toString())
				.name(entity.name)
				.description(entity.description)
				.build()
			).collect(toSet());
	}

	@Override
	public Set<GenericGroupAssignment> findAllBy(GenericGroupId id) {
		return genericGroupAssignmentEntityRepository.findAllByGenericGroupId(id.id).stream()
			.map(entity -> GenericGroupAssignment.builder()
				.id(entity.getId())
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
				.communityId(UUID.fromString(group.communityId))
				.name(group.name)
				.description(group.description)
				.build()
		);
		return new GenericGroupId(genericGroupEntity.getId());
	}

	@Override
	public GenericGroupAssignmentId create(GenericGroupAssignment assignment) {
		GenericGroupAssignmentEntity genericGroupAssignmentEntity = genericGroupAssignmentEntityRepository.save(
			GenericGroupAssignmentEntity.builder()
				.genericGroupId(assignment.genericGroupId.id)
				.userId(assignment.fenixUserId.id)
				.memberSince(assignment.utcMemberSince)
				.build()
		);
		return new GenericGroupAssignmentId(genericGroupAssignmentEntity.getId());
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
	public void delete(GenericGroupAssignmentId id) {
		genericGroupAssignmentEntityRepository.deleteById(id.id);
	}

	@Override
	public boolean existsBy(String communityId, GenericGroupId groupId) {
		return genericGroupEntityRepository.existsByCommunityIdAndId(UUID.fromString(communityId), groupId.id);
	}

	@Override
	public boolean existsBy(String communityId, GenericGroupAssignmentId assignmentId) {
		return genericGroupAssignmentEntityRepository.findByCommunityIdAndId(UUID.fromString(communityId), assignmentId.id)
			.isPresent();
	}

	@Override
	public boolean existsBy(GenericGroupId groupId, FenixUserId userId) {
		return genericGroupAssignmentEntityRepository.existsByGenericGroupIdAndUserId(groupId.id, userId.id);
	}

	@Override
	public boolean existsBy(String communityId, String name) {
		return genericGroupEntityRepository.existsByCommunityIdAndName(UUID.fromString(communityId), name);
	}
}

