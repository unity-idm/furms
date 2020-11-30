/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.time.ZonedDateTime;

class Project extends ProjectDefinition {
	
	final String id;

	public Project(String name, String description, ZonedDateTime validFrom,
			ZonedDateTime validTo, String researchField, User projectLeader,
			String communityId, String acronym, String id) {
		super(name, description, validFrom, validTo, researchField, projectLeader,
				communityId, acronym);
		this.id = id;
	}
}
