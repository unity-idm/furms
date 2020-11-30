/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.admin;

class SiteAllocatedResources {
	final ProjectAllocationId allocationId;
	final ResourceAmount amount;
	final Validity validity;
	
	SiteAllocatedResources(ProjectAllocationId allocationId, ResourceAmount amount, Validity validity) {
		this.allocationId = allocationId;
		this.amount = amount;
		this.validity = validity;
	}
}
