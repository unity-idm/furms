/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import java.util.List;
import java.util.Objects;

class UserUsage {
	final String email;
	final List<Double> usages;

	UserUsage(String email, List<Double> usages) {
		this.email = email;
		this.usages = usages;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserUsage userUsage = (UserUsage) o;
		return Objects.equals(email, userUsage.email) && Objects.equals(usages, userUsage.usages);
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, usages);
	}

	@Override
	public String toString() {
		return "UserUsage{" +
			"email='" + email + '\'' +
			", usages=" + usages +
			'}';
	}
}
