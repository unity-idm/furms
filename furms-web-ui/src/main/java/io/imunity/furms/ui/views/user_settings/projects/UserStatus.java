/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.projects;

public enum UserStatus {
	ACTIVE("view.user-settings.projects.filter.active", "view.user-settings.projects.grid.active"),
	REQUESTED("view.user-settings.projects.filter.requested", "view.user-settings.projects.grid.requested"),
	NOT_ACTIVE("view.user-settings.projects.filter.inactive", "view.user-settings.projects.grid.inactive");

	public final String filterText;
	public final String gridText;

	UserStatus(String filterText, String gridText) {
		this.filterText = filterText;
		this.gridText = gridText;
	}
}
