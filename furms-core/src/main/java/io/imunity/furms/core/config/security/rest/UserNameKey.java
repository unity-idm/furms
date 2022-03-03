/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.rest;

import java.util.Objects;

class UserNameKey {
	private final String keyName;

	UserNameKey(String keyName) {
		this.keyName = keyName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserNameKey that = (UserNameKey) o;
		return Objects.equals(keyName, that.keyName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(keyName);
	}

	@Override
	public String toString() {
		return "UserNameKey{" +
			"keyName='" + keyName + '\'' +
			'}';
	}
}
