/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation;

import io.imunity.furms.db.resource_types.ResourceTypeConverter;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProjectAllocationConverter {

	private final ResourceTypeConverter resourceTypeConverter;

	public ProjectAllocationConverter(ResourceTypeConverter resourceTypeConverter) {
		this.resourceTypeConverter = resourceTypeConverter;
	}

	public ProjectAllocationResolved toProjectAllocationResolved(ProjectAllocationReadEntity entity, BigDecimal consumed) {
		return ProjectAllocationResolved.builder()
				.id(entity.getId().toString())
				.site(entity.site.toSite())
				.resourceType(resourceTypeConverter.toResourceType(entity.resourceType))
				.resourceCredit(entity.resourceCredit.toResourceCredit())
				.communityAllocation(entity.communityAllocation.toCommunityAllocation())
				.projectId(entity.projectId.toString())
				.name(entity.name)
				.amount(entity.amount)
				.consumed(consumed)
				.build();
	}
}
