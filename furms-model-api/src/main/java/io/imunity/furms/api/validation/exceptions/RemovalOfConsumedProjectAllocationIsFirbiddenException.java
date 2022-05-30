/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;

public class RemovalOfConsumedProjectAllocationIsFirbiddenException extends IllegalArgumentException {
	public final ProjectAllocationId projectAllocationId;

	public RemovalOfConsumedProjectAllocationIsFirbiddenException(ProjectAllocationId projectAllocationId) {
		this.projectAllocationId = projectAllocationId;
	}
}
