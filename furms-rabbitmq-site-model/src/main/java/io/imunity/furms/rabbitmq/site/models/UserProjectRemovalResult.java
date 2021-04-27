/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("UserProjectRemovalResult")
public class UserProjectRemovalResult implements Body {
	public final String fenixUserId;
	public final String uid;
	public final String projectIdentifier;

	@JsonCreator
	UserProjectRemovalResult(String fenixUserId, String uid, String projectIdentifier) {
		this.fenixUserId = fenixUserId;
		this.uid = uid;
		this.projectIdentifier = projectIdentifier;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserProjectRemovalResult that = (UserProjectRemovalResult) o;
		return Objects.equals(fenixUserId, that.fenixUserId) &&
			Objects.equals(uid, that.uid) &&
			Objects.equals(projectIdentifier, that.projectIdentifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fenixUserId, uid, projectIdentifier);
	}

	@Override
	public String toString() {
		return "UserProjectRemovalResult{" +
			"fenixUserId='" + fenixUserId + '\'' +
			", uid='" + uid + '\'' +
			", projectIdentifier='" + projectIdentifier + '\'' +
			'}';
	}
}
