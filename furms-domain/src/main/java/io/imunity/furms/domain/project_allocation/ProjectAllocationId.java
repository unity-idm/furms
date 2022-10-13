/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class ProjectAllocationId extends UUIDBasedIdentifier {

	public ProjectAllocationId(String id) {
		super(id);
	}

	public ProjectAllocationId(UUID id) {
		super(id);
	}

	public ProjectAllocationId(ProjectAllocationId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "ProjectAllocationId{" + "id=" + id + '}';
	}
}
