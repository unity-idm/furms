/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.util.List;

class Site {
	
	final String id;
	final String name;
	final List<ResourceCredit> resourceCredits;
	final List<ResourceType> resourceTypes;
	final List<Service> services;
	final List<Policy> policies;
	
	Site(String id, String name, List<ResourceCredit> resourceCredits, List<ResourceType> resourceTypes,
			List<Service> services, List<Policy> policies) {
		this.id = id;
		this.name = name;
		this.resourceCredits = resourceCredits;
		this.resourceTypes = resourceTypes;
		this.services = services;
		this.policies = policies;
	}
}
