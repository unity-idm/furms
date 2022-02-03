/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import java.util.List;
import java.util.Objects;

public class UserUsage {
	public final String userEmail;
	public final List<Double> yUserUsageValues;

	UserUsage(String userEmail, List<Double> yUserUsageValues) {
		this.userEmail = userEmail;
		this.yUserUsageValues = yUserUsageValues;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserUsage userUsage = (UserUsage) o;
		return Objects.equals(userEmail, userUsage.userEmail) && Objects.equals(yUserUsageValues, userUsage.yUserUsageValues);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userEmail, yUserUsageValues);
	}

	@Override
	public String toString() {
		return "UserUsage{" +
			"email='" + userEmail + '\'' +
			", usages=" + yUserUsageValues +
			'}';
	}
}
