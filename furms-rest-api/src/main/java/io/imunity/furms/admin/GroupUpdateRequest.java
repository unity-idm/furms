/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class GroupUpdateRequest {
	final String name;

	final String description;

	GroupUpdateRequest(String name, String description) {
		this.name = name;
		this.description = description;
	}

}
