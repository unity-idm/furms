/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.site_agent;

public class IllegalCorrelationIdException extends IllegalArgumentException {
	public IllegalCorrelationIdException(String s) {
		super(s);
	}
}
