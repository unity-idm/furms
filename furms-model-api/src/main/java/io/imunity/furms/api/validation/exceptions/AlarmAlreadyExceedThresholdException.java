/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

public class AlarmAlreadyExceedThresholdException extends IllegalArgumentException {

	public AlarmAlreadyExceedThresholdException(String message) {
		super(message);
	}
}
