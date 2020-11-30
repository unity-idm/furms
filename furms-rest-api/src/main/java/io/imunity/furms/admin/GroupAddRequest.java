/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class GroupAddRequest {
	final String name;

	final String description;

	GroupAddRequest(String name, String description) {
		this.name = name;
		this.description = description;
	}

}
