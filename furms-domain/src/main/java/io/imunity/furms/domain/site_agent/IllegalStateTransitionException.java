/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.site_agent;

public class IllegalStateTransitionException extends IllegalArgumentException {
	public IllegalStateTransitionException(String s) {
		super(s);
	}
}
