/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.time.ZonedDateTime;

public class Validity {
	
	final ZonedDateTime from;
	final ZonedDateTime to;

	Validity(ZonedDateTime from, ZonedDateTime to) {
		this.from = from;
		this.to = to;
	}
}
