/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.projects;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class ProjectId extends UUIDBasedIdentifier {

	public ProjectId(String id) {
		super(id);
	}

	public ProjectId(UUID id) {
		super(id);
	}

	public ProjectId(ProjectId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "ProjectId{" + "id=" + id + '}';
	}
}
