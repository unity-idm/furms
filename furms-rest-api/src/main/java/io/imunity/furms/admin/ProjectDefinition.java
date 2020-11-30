/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.time.ZonedDateTime;

class ProjectDefinition extends ProjectMutableDefinition {
	
	final String communityId;
	final String acronym;
	
	public ProjectDefinition(String name, String description, ZonedDateTime validFrom,
			ZonedDateTime validTo, String researchField, User projectLeader,
			String communityId, String acronym) {
		super(name, description, validFrom, validTo, researchField, projectLeader);
		this.communityId = communityId;
		this.acronym = acronym;
	}
}
