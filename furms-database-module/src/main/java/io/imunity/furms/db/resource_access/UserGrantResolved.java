/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;

import java.util.Objects;

class UserGrantResolved {
	public final UserGrantEntity allocation;
	public final UserGrantJobEntity job;

	UserGrantResolved(UserGrantEntity allocation, UserGrantJobEntity job) {
		this.allocation = allocation;
		this.job = job;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserGrantResolved that = (UserGrantResolved) o;
		return Objects.equals(allocation, that.allocation) && Objects.equals(job, that.job);
	}

	@Override
	public int hashCode() {
		return Objects.hash(allocation, job);
	}

	@Override
	public String toString() {
		return "UserAllocationResolved{" +
			"allocation=" + allocation +
			", job=" + job +
			'}';
	}
}
