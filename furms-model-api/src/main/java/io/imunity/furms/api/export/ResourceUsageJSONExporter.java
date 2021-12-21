/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.export;

public interface ResourceUsageJSONExporter {
	String getJsonForProjectAllocation(String projectId, String projectAllocationId);
	String getJsonForCommunityAllocation(String communityId, String communityAllocationId);
}
