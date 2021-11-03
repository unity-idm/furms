/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_site_access;

public enum UserSiteAccessStatus {
	ENABLED(true, true, false),
	ENABLING_PENDING(true, false, true),
	DISABLING_PENDING(false, true, true),
	ENABLING_FAILED(false, false, false),
	DISABLING_FAILED(true, true, false),
	DISABLED(false, false, false);

	private final boolean enabled;
	private final boolean installed;
	private final boolean pending;

	UserSiteAccessStatus(boolean enabled, boolean installed, boolean pending) {
		this.enabled = enabled;
		this.installed = installed;
		this.pending = pending;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isInstalled() {
		return installed;
	}

	public boolean isPending() {
		return pending;
	}
}
