/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class ProjectMutableDefinition {

	final String name;
	final String description;
	final Validity validity;
	final String researchField;
	final User projectLeader;
	
	ProjectMutableDefinition(String name, String description, Validity validity, 
			String researchField, User projectLeader) {
		this.name = name;
		this.description = description;
		this.validity = validity;
		this.researchField = researchField;
		this.projectLeader = projectLeader;
	}
}
