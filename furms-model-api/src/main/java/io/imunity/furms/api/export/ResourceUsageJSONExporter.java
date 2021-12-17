/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.export;

public interface ResourceUsageJSONExporter {
	byte[] getJsonFileForProjectAllocation(String projectId, String projectAllocationId);
	byte[] getJsonFileForCommunityAllocation(String communityId, String communityAllocationId);
}
