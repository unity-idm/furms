/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

class ProjectDefinition extends ProjectMutableDefinition {
	
	final String communityId;
	final String acronym;
	final String gid;

	public ProjectDefinition(String name, String description, Validity validity,
			String researchField, User projectLeader,
			String communityId, String acronym, String gid) {
		super(name, description, validity, researchField, projectLeader);
		this.communityId = communityId;
		this.acronym = acronym;
		this.gid = gid;
	}
}
