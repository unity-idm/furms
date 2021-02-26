/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.Objects;

public class FurmsViewUserModel {
	public static final FurmsViewUserModel EMPTY = new FurmsViewUserModel(null, "", "", "");

	public final PersistentId id;
	public final String firstname;
	public final String lastname;
	public final String email;

	public FurmsViewUserModel(FURMSUser user) {
		this(user.id, user.firstName, user.lastName, user.email);
	}
	
	public FurmsViewUserModel(PersistentId id, String firstname, String lastname, String email) {
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
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
