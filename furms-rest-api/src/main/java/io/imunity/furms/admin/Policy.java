/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class Policy {
	final PolicyId id;

	final String name;

	final int revision;

	Policy(PolicyId id, String name, int revision) {
		this.id = id;
		this.name = name;
		this.revision = revision;
	}
}
