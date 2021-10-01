/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.generic_groups.GenericGroupService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.rest.error.exceptions.CommunityAllocationRestNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Service
class CommunityRestService {

	private final CommunityService communityService;
	private final ProjectService projectService;
	private final CommunityAllocationService communityAllocationService;
	private final ResourceChecker resourceChecker;
	private final ProjectsRestConverter projectsRestConverter;
	private final GenericGroupService genericGroupService;

	CommunityRestService(CommunityService communityService,
	                     ProjectService projectService,
	                     CommunityAllocationService communityAllocationService,
	                     ProjectsRestConverter projectsRestConverter,
	                     GenericGroupService genericGroupService) {
		this.communityService = communityService;
		this.projectService = projectService;
		this.communityAllocationService = communityAllocationService;
		this.resourceChecker = new ResourceChecker(communityService::existsById);
		this.projectsRestConverter = projectsRestConverter;
		this.genericGroupService = genericGroupService;
	}

	List<Community> findAll() {
		return communityService.findAllOfCurrentUser().stream()
				.map(community -> new Community(
						community,
						communityAllocationService.findAllByCommunityId(community.getId())))
				.collect(toList());
	}

	Community findOneById(String communityId) {
		return resourceChecker.performIfExists(communityId, () -> communityService.findById(communityId))
				.map(community -> new Community(
						community,
						communityAllocationService.findAllByCommunityId(communityId)))
				.get();
	}

	List<Project> findAllProjectsByCommunityId(String communityId) {
		return resourceChecker.performIfExists(communityId, () -> projectService.findAllByCommunityId(communityId))
				.stream()
				.map(projectsRestConverter::convert)
				.collect(toList());
	}

	List<CommunityAllocation> findAllocationByCommunityId(String communityId) {
		resourceChecker.performIfExists(communityId, () -> communityService.findById(communityId));
		return communityAllocationService.findAllWithRelatedObjects(communityId).stream()
				.map(CommunityAllocation::new)
				.collect(toList());
	}

	CommunityAllocation findAllocationByIdAndCommunityId(String communityAllocationId, String communityId) {
		return resourceChecker.performIfExists(communityId,
					() -> communityAllocationService.findByCommunityIdAndIdWithRelatedObjects(communityId, communityAllocationId))
				.filter(allocation -> allocation.communityId.equals(communityId))
				.map(CommunityAllocation::new)
				.orElseThrow(() -> new CommunityAllocationRestNotFoundException(format(
						"Could not locate Community Allocation for provided id=%s and communityId=%s",
						communityAllocationId, communityId)));
	}

	List<CommunityAllocation> addAllocation(String communityId, CommunityAllocationAddRequest request) {
		resourceChecker.performIfExists(communityId, () -> communityService.findById(communityId));
		communityAllocationService.create(io.imunity.furms.domain.community_allocation.CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(request.creditId)
				.name(request.name)
				.amount(request.amount)
				.build());
		return findAllocationByCommunityId(communityId);
	}

	List<Group> getGroups(String communityId){
		return resourceChecker.performIfExists(communityId, () -> genericGroupService.findAll(communityId)).stream()
			.map(group -> new Group(group.id.id.toString(), group.name, group.description))
			.collect(toList());
	}

	GroupWithMembers getGroupWithMember(String communityId, String groupId){
		return resourceChecker.performIfExists(communityId, () -> genericGroupService.findGroupWithAssignments(communityId, new GenericGroupId(groupId)))
			.map(group -> new GroupWithMembers(
				group.group.id.id.toString(),
				group.group.name, group.group.description,
				group.memberships.stream()
					.map(x -> x.fenixUserId.id)
					.collect(toList())))
			.get();
	}

	void deleteGroup(String communityId, String groupId){
		resourceChecker.performIfExists(communityId, () -> genericGroupService.findBy(communityId, new GenericGroupId(groupId)));
		genericGroupService.delete(communityId, new GenericGroupId(UUID.fromString(groupId)));
	}

	Group updateGroup(String communityId, String groupId, GroupDefinitionRequest request){
		resourceChecker.performIfExists(communityId, () -> genericGroupService.findBy(communityId, new GenericGroupId(groupId)));
		genericGroupService.update(GenericGroup.builder()
			.id(UUID.fromString(groupId))
			.communityId(communityId)
			.name(request.name)
			.description(request.description)
			.build());
		return new Group(groupId, request.name, request.description);
	}

	Group addGroup(String communityId, GroupDefinitionRequest request){
		GenericGroupId genericGroupId = genericGroupService.create(GenericGroup.builder()
			.communityId(communityId)
			.name(request.name)
			.description(request.description)
			.build());
		return new Group(genericGroupId.id.toString(), request.name, request.description);
	}
}
