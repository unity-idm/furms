/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.export;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;

import java.util.function.Supplier;

public interface ResourceUsageCSVExporter {
	Supplier<String> getCsvForProjectAllocation(ProjectId projectId, ProjectAllocationId projectAllocationId);
	Supplier<String> getCsvForCommunityAllocation(CommunityId communityId, CommunityAllocationId communityAllocationId);
}
