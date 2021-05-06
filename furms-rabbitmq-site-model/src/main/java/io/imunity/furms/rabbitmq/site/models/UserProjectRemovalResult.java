/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("UserProjectRemovalResult")
public class UserProjectRemovalResult implements Body {

	@JsonCreator
	public UserProjectRemovalResult() {
	}

	@Override
	public String toString() {
		return "UserProjectRemovalResult{}";
	}
}
