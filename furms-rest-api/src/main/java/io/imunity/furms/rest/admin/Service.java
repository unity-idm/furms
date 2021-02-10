/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

class Service {
	final ServiceId id;

	final String name;

	final PolicyId policyId;

	Service(ServiceId id, String name, PolicyId policyId) {
		this.id = id;
		this.name = name;
		this.policyId = policyId;
	}

}
