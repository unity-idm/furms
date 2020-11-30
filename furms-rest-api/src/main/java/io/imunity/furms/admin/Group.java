/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class Group {
	final String id;

	final String name;

	final String description;

	Group(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

}
