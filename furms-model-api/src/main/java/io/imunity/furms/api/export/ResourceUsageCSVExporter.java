/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.export;

public interface ResourceUsageCSVExporter {
	String getCsvForProjectAllocation(String projectId, String projectAllocationId);
	String getCsvForCommunityAllocation(String communityId, String communityAllocationId);
}
