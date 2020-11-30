/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class ProjectDefinition extends ProjectMutableDefinition {
	
	final String communityId;

	ProjectDefinition(String communityId, String name, String description) {
		super(name, description);
		this.communityId = communityId;
	}
}
