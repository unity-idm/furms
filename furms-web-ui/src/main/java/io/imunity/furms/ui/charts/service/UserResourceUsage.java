/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserResourceUsage {
	public final String userEmail;
	public final List<Double> yUserCumulativeUsageValues;

	UserResourceUsage(String userEmail, List<Double> yUserCumulativeUsageValues) {
		this.userEmail = userEmail;
		this.yUserCumulativeUsageValues = Collections.unmodifiableList(yUserCumulativeUsageValues);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserResourceUsage userResourceUsage = (UserResourceUsage) o;
		return Objects.equals(userEmail, userResourceUsage.userEmail) && Objects.equals(yUserCumulativeUsageValues, userResourceUsage.yUserCumulativeUsageValues);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userEmail, yUserCumulativeUsageValues);
	}

	@Override
	public String toString() {
		return "UserUsage{" +
			"email='" + userEmail + '\'' +
			", usages=" + yUserCumulativeUsageValues +
			'}';
	}
}
