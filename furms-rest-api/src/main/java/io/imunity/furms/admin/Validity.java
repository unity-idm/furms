/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.time.Instant;

public class Validity {
	final Instant from;

	final Instant to;

	Validity(Instant from, Instant to) {
		this.from = from;
		this.to = to;
	}

}
