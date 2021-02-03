/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import java.util.Objects;

public class FurmsViewUserModel {
	public final String id;
	public final String firstname;
	public final String lastname;
	public final String email;

	public FurmsViewUserModel(String id, String firstname, String lastname, String email) {
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
	}

	public FurmsViewUserModel(String id) {
		this.id = id;
		this.firstname = null;
		this.lastname = null;
		this.email = null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FurmsViewUserModel that = (FurmsViewUserModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
