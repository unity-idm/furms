/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.site_agent;

import java.util.Objects;
import java.util.UUID;

public class CorrelationId {
	public final String id;

	public CorrelationId(String id) {
		this.id = id;
	}

	public CorrelationId() {
		this.id = UUID.randomUUID().toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CorrelationId that = (CorrelationId) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "CorrelationId{" +
			"id='" + id + '\'' +
			'}';
	}
}
