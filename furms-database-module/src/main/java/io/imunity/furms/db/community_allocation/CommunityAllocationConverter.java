/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.community_allocation;

import io.imunity.furms.db.resource_types.ResourceTypeConverter;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import org.springframework.stereotype.Component;

@Component
public class CommunityAllocationConverter {

	private final ResourceTypeConverter resourceTypeConverter;

	public CommunityAllocationConverter(ResourceTypeConverter resourceTypeConverter) {
		this.resourceTypeConverter = resourceTypeConverter;
	}

	CommunityAllocationResolved toCommunityAllocationResolved(CommunityAllocationReadEntity entity) {
		return CommunityAllocationResolved.builder()
				.id(entity.getId().toString())
				.site(entity.site.toSite())
				.resourceType(resourceTypeConverter.toResourceType(entity.resourceType))
				.resourceCredit(entity.resourceCredit.toResourceCredit())
				.communityId(entity.communityId.toString())
				.communityName(entity.communityName)
				.name(entity.name)
				.amount(entity.amount)
				.build();
	}

	CommunityAllocation toCommunityAllocation(CommunityAllocationReadEntity entity) {
		return CommunityAllocation.builder()
				.id(entity.getId().toString())
				.resourceCreditId(entity.resourceCredit.getId().toString())
				.communityId(entity.communityId.toString())
				.name(entity.name)
				.amount(entity.amount)
				.build();
	}

}
