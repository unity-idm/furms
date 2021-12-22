/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.export;

import java.util.function.Supplier;

public interface ResourceUsageCSVExporter {
	Supplier<String> getCsvForProjectAllocation(String projectId, String projectAllocationId);
	Supplier<String> getCsvForCommunityAllocation(String communityId, String communityAllocationId);
}
