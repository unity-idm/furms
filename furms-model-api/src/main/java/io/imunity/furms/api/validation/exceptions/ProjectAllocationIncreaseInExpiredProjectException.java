/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

public class ProjectAllocationIncreaseInExpiredProjectException extends IllegalArgumentException {

	public ProjectAllocationIncreaseInExpiredProjectException(String message) {
		super(message);
	}

	public ProjectAllocationIncreaseInExpiredProjectException() {
	}
}
