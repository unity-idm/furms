/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class ResourceCredit {
	
	final SiteAllocationId id;
	final String name;
	final Validity validity;
	final String resourceTypeId;
	final ResourceAmount amount;

	ResourceCredit(SiteAllocationId id, String name, Validity validity, String resourceTypeId,
			ResourceAmount amount) {
		this.id = id;
		this.name = name;
		this.validity = validity;
		this.resourceTypeId = resourceTypeId;
		this.amount = amount;
	}

}
