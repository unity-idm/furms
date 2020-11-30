/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class SiteAllocation {
	final SiteAllocationId id;

	final String name;

	final Validity validity;

	final String resourceTypeId;

	final ResourceCredit credits;

	SiteAllocation(SiteAllocationId id, String name, Validity validity, String resourceTypeId,
			ResourceCredit credits) {
		this.id = id;
		this.name = name;
		this.validity = validity;
		this.resourceTypeId = resourceTypeId;
		this.credits = credits;
	}

}
