/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;

import java.util.Objects;
import java.util.Optional;

public class FurmsViewUserModel {
	public static final FurmsViewUserModel EMPTY = new FurmsViewUserModel(null, Optional.empty(), Optional.empty(), Optional.empty(), "");

	public final Optional<PersistentId> id;
	public final Optional<FenixUserId> fenixUserId;
	public final Optional<String> firstname;
	public final Optional<String> lastname;
	public final String email;

	public FurmsViewUserModel(FURMSUser user) {
		this(user.id, user.fenixUserId, user.firstName, user.lastName, user.email);
	}
	
	public FurmsViewUserModel(Optional<PersistentId> id, Optional<FenixUserId> fenixUserId, Optional<String> firstname,
	                          Optional<String> lastname, String email) {
		this.id = id;
		this.fenixUserId = fenixUserId;
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
