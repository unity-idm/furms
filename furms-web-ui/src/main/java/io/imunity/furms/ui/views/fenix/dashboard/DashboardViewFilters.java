/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.dashboard;

class DashboardViewFilters {
	private String name;
	private boolean includeFullyDistributed;
	private boolean includeExpired;

	DashboardViewFilters() {
		this.name = "";
		this.includeFullyDistributed = false;
		this.includeExpired = false;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	boolean isIncludeFullyDistributed() {
		return includeFullyDistributed;
	}

	void setIncludeFullyDistributed(boolean includeFullyDistributed) {
		this.includeFullyDistributed = includeFullyDistributed;
	}

	boolean isIncludeExpired() {
		return includeExpired;
	}

	void setIncludeExpired(boolean includeExpired) {
		this.includeExpired = includeExpired;
	}

}
