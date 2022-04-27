/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.generic_groups.GenericGroupService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.rest.error.exceptions.CommunityAllocationRestNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

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
	private final ResourceCreditService resourceCreditService;

	CommunityRestService(CommunityService communityService,
	                     ProjectService projectService,
	                     CommunityAllocationService communityAllocationService,
	                     ProjectsRestConverter projectsRestConverter,
	                     GenericGroupService genericGroupService,
	                     ResourceCreditService resourceCreditService) {
		this.communityService = communityService;
		this.projectService = projectService;
		this.communityAllocationService = communityAllocationService;
		this.resourceChecker = new ResourceChecker(id -> communityService.existsById(new CommunityId(id)));
		this.projectsRestConverter = projectsRestConverter;
		this.genericGroupService = genericGroupService;
		this.resourceCreditService = resourceCreditService;
	}

	List<Community> findAll() {
		return communityService.findAllOfCurrentUser().stream()
				.map(community -> new Community(
						community,
						communityAllocationService.findAllByCommunityId(community.getId())))
				.collect(toList());
	}

	Community findOneById(CommunityId communityId) {
		return resourceChecker.performIfExists(communityId.id, () -> communityService.findById(communityId))
				.map(community -> new Community(
						community,
						communityAllocationService.findAllByCommunityId(communityId)))
				.get();
	}

	List<Project> findAllProjectsByCommunityId(CommunityId communityId) {
		return resourceChecker.performIfExists(communityId.id, () -> projectService.findAllByCommunityId(communityId))
				.stream()
				.map(projectsRestConverter::convert)
				.collect(toList());
	}

	List<CommunityAllocation> findAllocationByCommunityId(CommunityId communityId) {
		resourceChecker.performIfExists(communityId.id, () -> communityService.findById(communityId));
		return communityAllocationService.findAllWithRelatedObjects(communityId).stream()
				.map(CommunityAllocation::new)
				.collect(toList());
	}

	CommunityAllocation findAllocationByIdAndCommunityId(CommunityAllocationId communityAllocationId, CommunityId communityId) {
		return resourceChecker.performIfExists(communityId.id,
					() -> communityAllocationService.findByCommunityIdAndIdWithRelatedObjects(communityId, communityAllocationId))
				.filter(allocation -> allocation.communityId.equals(communityId))
				.map(CommunityAllocation::new)
				.orElseThrow(() -> new CommunityAllocationRestNotFoundException(format(
						"Could not locate Community Allocation for provided id=%s and communityId=%s",
						communityAllocationId, communityId)));
	}

	List<CommunityAllocation> addAllocation(CommunityId communityId, CommunityAllocationAddRequest request) {
		resourceChecker.performIfExists(communityId.id, () -> communityService.findById(communityId));
		resourceChecker.assertUUID(request.creditId);
		communityAllocationService.create(io.imunity.furms.domain.community_allocation.CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(request.creditId)
				.name(request.name)
				.amount(request.amount)
				.build());
		return findAllocationByCommunityId(communityId);
	}

	List<Group> getGroups(CommunityId communityId){
		return resourceChecker.performIfExists(communityId.id, () -> genericGroupService.findAll(communityId)).stream()
			.map(group -> new Group(group.id.id.toString(), group.name, group.description))
			.collect(toList());
	}

	GroupWithMembers getGroupWithMember(CommunityId communityId, GenericGroupId groupId){
		return resourceChecker.performIfExists(communityId.id,
				() -> genericGroupService.findGroupWithAssignments(communityId, groupId))
			.map(group -> new GroupWithMembers(
				group.group.id.id.toString(),
				group.group.name, group.group.description,
				group.memberships.stream()
					.map(x -> x.fenixUserId.id)
					.collect(toList())))
			.get();
	}

	void deleteGroup(CommunityId communityId, GenericGroupId groupId){
		resourceChecker.performIfExists(communityId.id, () -> genericGroupService.findBy(communityId,
			groupId));
		genericGroupService.delete(communityId, groupId);
	}

	Group updateGroup(CommunityId communityId, GenericGroupId groupId, GroupDefinitionRequest request){
		resourceChecker.performIfExists(communityId.id, () -> genericGroupService.findBy(communityId, groupId));
		genericGroupService.update(GenericGroup.builder()
			.id(groupId)
			.communityId(communityId)
			.name(request.name)
			.description(request.description)
			.build());
		return new Group(groupId.id.toString(), request.name, request.description);
	}

	Group addGroup(CommunityId communityId, GroupDefinitionRequest request){
		GenericGroupId genericGroupId = genericGroupService.create(GenericGroup.builder()
			.communityId(communityId)
			.name(request.name)
			.description(request.description)
			.build());
		return new Group(genericGroupId.id.toString(), request.name, request.description);
	}
}
