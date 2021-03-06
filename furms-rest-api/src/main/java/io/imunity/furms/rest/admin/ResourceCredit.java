/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

class ResourceCredit {
	
	final SiteCreditId id;
	final String name;
	final Validity validity;
	final String resourceTypeId;
	final ResourceAmount amount;

	ResourceCredit(SiteCreditId id, String name, Validity validity, String resourceTypeId,
			ResourceAmount amount) {
		this.id = id;
		this.name = name;
		this.validity = validity;
		this.resourceTypeId = resourceTypeId;
		this.amount = amount;
	}

}
