/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.util.List;

class Project extends ProjectDefinition {
	
	final String id;

	Project(String id, String name, String description, List<String> allocations,
			String communityId) {
		super(communityId, name, description);
		this.id = id;
	}
}
