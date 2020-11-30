/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class Group extends GroupDefinition {

	final String id;

	Group(String id, String name, String description) {
		super(name, description);
		this.id = id;
	}
}
