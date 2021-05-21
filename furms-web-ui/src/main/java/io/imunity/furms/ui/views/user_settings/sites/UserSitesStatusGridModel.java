/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.sites;

import java.util.Objects;

public class UserSitesStatusGridModel {

	private String state;
	private String message;

	public UserSitesStatusGridModel(String state, String message) {
		this.state = state;
		this.message = message;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserSitesStatusGridModel that = (UserSitesStatusGridModel) o;
		return Objects.equals(state, that.state) &&
				Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(state, message);
	}

	@Override
	public String toString() {
		return "UserSitesStatusGridModel{" +
				"state='" + state + '\'' +
				", message='" + message + '\'' +
				'}';
	}
}
