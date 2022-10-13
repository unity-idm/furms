/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class ProjectInstallationId extends UUIDBasedIdentifier {

	public ProjectInstallationId(String id) {
		super(id);
	}

	public ProjectInstallationId(UUID id) {
		super(id);
	}

	public ProjectInstallationId(ProjectInstallationId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "ProjectInstallationId{" + "id=" + id + '}';
	}
}
