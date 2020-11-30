/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class ProjectUpdateRequest {
	final String name;

	final String description;

	ProjectUpdateRequest(String name, String description) {
		this.name = name;
		this.description = description;
	}

}
