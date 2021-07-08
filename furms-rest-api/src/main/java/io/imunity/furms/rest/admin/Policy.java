/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class Policy {
	public final PolicyId id;
	public final String name;
	public final int revision;

	Policy(PolicyId id, String name, int revision) {
		this.id = id;
		this.name = name;
		this.revision = revision;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Policy policy = (Policy) o;
		return revision == policy.revision
				&& Objects.equals(id, policy.id)
				&& Objects.equals(name, policy.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, revision);
	}

	@Override
	public String toString() {
		return "Policy{" +
				"id=" + id +
				", name='" + name + '\'' +
				", revision=" + revision +
				'}';
	}
}
