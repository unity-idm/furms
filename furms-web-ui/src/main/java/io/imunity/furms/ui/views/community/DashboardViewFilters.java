/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community;

class DashboardViewFilters {
	private boolean includedFullyDistributed;
	private boolean includedExpired;

	DashboardViewFilters() {
		this.includedFullyDistributed = false;
		this.includedExpired = false;
	}

	boolean isIncludedFullyDistributed() {
		return includedFullyDistributed;
	}

	void setIncludedFullyDistributed(boolean includedFullyDistributed) {
		this.includedFullyDistributed = includedFullyDistributed;
	}

	boolean isIncludedExpired() {
		return includedExpired;
	}

	void setIncludedExpired(boolean includedExpired) {
		this.includedExpired = includedExpired;
	}

	static class Checkboxes {
		enum Options {
			INCLUDED_FULLY_DISTRIBUTED,
			INCLUDED_EXPIRED;
		}

		private final Options option;
		private final String label;

		Checkboxes(Options option, String label) {
			this.option = option;
			this.label = label;
		}

		Options getOption() {
			return option;
		}

		String getLabel() {
			return label;
		}
	}

}
