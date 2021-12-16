/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import java.util.List;

class UserUsage {
	final String email;
	final List<Double> usages;

	UserUsage(String email, List<Double> usages) {
		this.email = email;
		this.usages = usages;
	}
}
