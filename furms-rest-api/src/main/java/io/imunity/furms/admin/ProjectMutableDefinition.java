/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.time.ZonedDateTime;

class ProjectMutableDefinition {

	final String name;
	final String description;
	final ZonedDateTime validFrom;
	final ZonedDateTime validTo;
	final String researchField;
	final User projectLeader;
	
	ProjectMutableDefinition(String name, String description, ZonedDateTime validFrom,
			ZonedDateTime validTo, String researchField, User projectLeader) {
		this.name = name;
		this.description = description;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.researchField = researchField;
		this.projectLeader = projectLeader;
	}
}
