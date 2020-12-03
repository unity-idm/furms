/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.admin;

import java.time.ZonedDateTime;

class ProjectCumulativeResourceConsumption {
	
	final ProjectAllocationId projectAllocationId;
	final ResourceTypeId resourceTypeId;
	final ResourceAmount consumedAmount;
	final ZonedDateTime consumedUntil;
	
	ProjectCumulativeResourceConsumption(ProjectAllocationId projectAllocationId,
			ResourceTypeId resourceTypeId, ResourceAmount consumedAmount, ZonedDateTime consumedUntil) {
		this.projectAllocationId = projectAllocationId;
		this.resourceTypeId = resourceTypeId;
		this.consumedAmount = consumedAmount;
		this.consumedUntil = consumedUntil;
	}
}
