/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.rest.error.exceptions.CommunityAllocationRestNotFoundException;

@Service
class CommunityRestService {

	private final CommunityService communityService;
	private final CommunityAllocationService communityAllocationService;
	private final ResourceChecker resourceChecker;

	CommunityRestService(CommunityService communityService, CommunityAllocationService communityAllocationService) {
		this.communityService = communityService;
		this.communityAllocationService = communityAllocationService;
		this.resourceChecker = new ResourceChecker(communityService::existsById);
	}

	List<Community> findAll() {
		return communityService.findAll().stream()
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

	List<CommunityAllocation> findAllocationByCommunityId(String communityId) {
		resourceChecker.performIfExists(communityId, () -> communityService.findById(communityId));
		return communityAllocationService.findAllWithRelatedObjects(communityId).stream()
				.map(CommunityAllocation::new)
				.collect(toList());
	}

	CommunityAllocation findAllocationByIdAndCommunityId(String communityAllocationId, String communityId) {
		return resourceChecker.performIfExists(communityId,
					() -> communityAllocationService.findByIdWithRelatedObjects(communityAllocationId))
				.filter(allocation -> allocation.communityId.equals(communityId))
				.map(CommunityAllocation::new)
				.orElseThrow(() -> new CommunityAllocationRestNotFoundException(format(
						"Could not locate Community Allocation for provided id=%s and communityId=%s",
						communityAllocationId, communityId)));
	}

	List<CommunityAllocation> addAllocation(String communityId, CommunityAllocationAddRequest request) {
		communityAllocationService.create(io.imunity.furms.domain.community_allocation.CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(request.creditId)
				.name(request.name)
				.amount(request.amount)
				.build());
		return findAllocationByCommunityId(communityId);
	}

}
