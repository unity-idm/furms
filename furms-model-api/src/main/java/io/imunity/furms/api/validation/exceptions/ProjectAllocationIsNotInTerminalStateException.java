/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

public class ProjectAllocationIsNotInTerminalStateException extends IllegalArgumentException {
	public final String projectAllocationId;
	public ProjectAllocationIsNotInTerminalStateException(String projectAllocationId) {
		super("projectAllocationId: " + projectAllocationId);
		this.projectAllocationId = projectAllocationId;
	}
}
