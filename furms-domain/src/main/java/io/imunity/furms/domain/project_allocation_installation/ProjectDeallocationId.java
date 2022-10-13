/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class ProjectDeallocationId extends UUIDBasedIdentifier {

	public ProjectDeallocationId(String id) {
		super(id);
	}

	public ProjectDeallocationId(UUID id) {
		super(id);
	}

	public ProjectDeallocationId(ProjectDeallocationId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "ProjectDeallocationId{" + "id=" + id + '}';
	}
}
