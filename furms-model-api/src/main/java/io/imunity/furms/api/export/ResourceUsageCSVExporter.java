/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.export;

public interface ResourceUsageCSVExporter {
	byte[] getCsvFileForProjectAllocation(String projectId, String projectAllocationId);
	byte[] getCsvFileForCommunityAllocation(String communityId, String communityAllocationId);
}
