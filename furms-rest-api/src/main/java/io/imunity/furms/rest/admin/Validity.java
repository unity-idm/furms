/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.time.ZonedDateTime;
import java.util.Objects;

class Validity {
	public final ZonedDateTime from;
	public final ZonedDateTime to;

	Validity(ZonedDateTime from, ZonedDateTime to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Validity validity = (Validity) o;
		return Objects.equals(from.toInstant(), validity.from.toInstant())
				&& Objects.equals(to.toInstant(), validity.to.toInstant());
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to);
	}

	@Override
	public String toString() {
		return "Validity{" +
				"from=" + from +
				", to=" + to +
				'}';
	}
}
