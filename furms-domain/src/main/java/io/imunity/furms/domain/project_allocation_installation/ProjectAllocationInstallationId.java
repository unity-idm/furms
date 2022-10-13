/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class ProjectAllocationInstallationId extends UUIDBasedIdentifier {

	public ProjectAllocationInstallationId(String id) {
		super(id);
	}

	public ProjectAllocationInstallationId(UUID id) {
		super(id);
	}

	public ProjectAllocationInstallationId(ProjectAllocationInstallationId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "ProjectAllocationInstallationId{" + "id=" + id + '}';
	}
}
