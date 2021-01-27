/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import java.util.Objects;

public class FurmsViewUserContext {
	public final String id;
	public final String name;
	public final String route;
	public final ViewMode viewMode;
	public final boolean redirectable;

	public FurmsViewUserContext(String id, String name, ViewMode viewMode) {
		this.id = id;
		this.name = name;
		this.route = viewMode.route;
		this.viewMode = viewMode;
		this.redirectable = true;
	}

	public FurmsViewUserContext(String id, String name, ViewMode viewMode, String route) {
		this.id = id;
		this.name = name;
		this.route = route;
		this.viewMode = viewMode;
		this.redirectable = true;
	}

	public FurmsViewUserContext(FurmsViewUserContext furmsViewUserContext, boolean redirectable) {
		this.id = furmsViewUserContext.id;
		this.name = furmsViewUserContext.name;
		this.route = furmsViewUserContext.route;
		this.viewMode = furmsViewUserContext.viewMode;
		this.redirectable = redirectable;
	}

	public FurmsViewUserContext(String name, ViewMode viewMode) {
		this(null, name, viewMode);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FurmsViewUserContext that = (FurmsViewUserContext) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(name, that.name) &&
			Objects.equals(route, that.route) &&
			viewMode == that.viewMode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, route, viewMode);
	}

	@Override
	public String toString() {
		return "FurmsViewUserContext{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", route='" + route + '\'' +
			", viewMode=" + viewMode +
			'}';
	}
}
