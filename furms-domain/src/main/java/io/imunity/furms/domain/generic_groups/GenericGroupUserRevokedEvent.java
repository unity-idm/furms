/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.users.FURMSUser;

import java.util.Objects;

public class GenericGroupUserRevokedEvent implements FurmsEvent {
	public final FURMSUser user;
	public final GenericGroup group;

	public GenericGroupUserRevokedEvent(FURMSUser user, GenericGroup group) {
		this.user = user;
		this.group = group;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupUserRevokedEvent userEvent = (GenericGroupUserRevokedEvent) o;
		return Objects.equals(group, userEvent.group) &&
			Objects.equals(user, userEvent.user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(group, user);
	}

	@Override
	public String toString() {
		return "GenericGroupUserRevokedEvent{" +
			"user='" + user + '\'' +
			",group='" + group + '\'' +
			'}';
	}
}
