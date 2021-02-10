/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.time.ZonedDateTime;

class ProjectUsageRecord {
	
	final ProjectAllocationId projectAllocationId;
	final ResourceTypeId resourceTypeId;
	final ResourceAmount consumedAmount;
	final String userFenixId;
	final ZonedDateTime from;
	final ZonedDateTime until;
	
	ProjectUsageRecord(ProjectAllocationId projectAllocationId, ResourceTypeId resourceTypeId,
			ResourceAmount consumedAmount, String userFenixId, ZonedDateTime from, ZonedDateTime until) {
		this.projectAllocationId = projectAllocationId;
		this.resourceTypeId = resourceTypeId;
		this.consumedAmount = consumedAmount;
		this.userFenixId = userFenixId;
		this.from = from;
		this.until = until;
	}
}
