/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.users.FURMSUser;

import java.util.Objects;

public class GenericGroupUserGrantedEvent implements FurmsEvent {
	public final FURMSUser user;
	public final GenericGroup group;

	public GenericGroupUserGrantedEvent(FURMSUser user, GenericGroup group) {
		this.user = user;
		this.group = group;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupUserGrantedEvent userEvent = (GenericGroupUserGrantedEvent) o;
		return Objects.equals(group, userEvent.group) &&
			Objects.equals(user, userEvent.user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(group, user);
	}

	@Override
	public String toString() {
		return "GenericGroupUserGrantedEvent{" +
			"user='" + user + '\'' +
			",group='" + group + '\'' +
			'}';
	}
}
