/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

class Project extends ProjectDefinition {
	
	final String id;

	public Project(String name, String description, Validity validity, String researchField, User projectLeader,
			String communityId, String acronym, String id) {
		super(name, description, validity, researchField, projectLeader,
				communityId, acronym);
		this.id = id;
	}
}
