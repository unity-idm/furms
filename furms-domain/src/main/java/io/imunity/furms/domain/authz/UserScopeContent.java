/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.authz;

import java.util.Objects;

public class UserScopeContent {
	public final String id;
	public final String name;
	public final String redirectURI;

	public UserScopeContent(String id, String name, String redirectURI) {
		this.id = id;
		this.name = name;
		this.redirectURI = redirectURI;
	}

	public UserScopeContent(String name) {
		this.id = null;
		this.name = name;
		this.redirectURI = null;
	}

	public UserScopeContent(String name, String redirectURI) {
		this.id = null;
		this.name = name;
		this.redirectURI = redirectURI;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserScopeContent that = (UserScopeContent) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(name, that.name) &&
			Objects.equals(redirectURI, that.redirectURI);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, redirectURI);
	}

	@Override
	public String toString() {
		return "FurmsDisplayContener{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", redirectURI='" + redirectURI + '\'' +
			'}';
	}
}
