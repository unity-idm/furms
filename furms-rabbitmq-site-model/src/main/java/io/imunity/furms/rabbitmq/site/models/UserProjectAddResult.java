/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("UserProjectAddResult")
public class UserProjectAddResult implements Body {
	public final String uid;

	@JsonCreator
	public UserProjectAddResult(String uid) {
		this.uid = uid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserProjectAddResult that = (UserProjectAddResult) o;
		return Objects.equals(uid, that.uid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uid);
	}

	@Override
	public String toString() {
		return "UserProjectAddResult{" +
			", uid='" + uid + '\'' +
			'}';
	}
}
