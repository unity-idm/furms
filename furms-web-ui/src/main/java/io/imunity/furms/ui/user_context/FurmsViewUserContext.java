/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import java.util.Objects;

import com.google.common.base.Preconditions;

public class FurmsViewUserContext {
	public final String id;
	public final String name;
	public final String route;
	public final ViewMode viewMode;
	public final boolean redirectable;
	
	private FurmsViewUserContext(String id, String name, String route, ViewMode viewMode, boolean redirectable) {
		Preconditions.checkNotNull(id);
		this.id = id;
		this.name = name;
		this.route = route;
		this.viewMode = viewMode;
		this.redirectable = redirectable;
	}

	public FurmsViewUserContext(String id, String name, ViewMode viewMode) {
		this(id, name, viewMode.route, viewMode, true);
	}

	public FurmsViewUserContext(String id, String name, ViewMode viewMode, String route) {
		this(id, name, route, viewMode, true);
	}

	public FurmsViewUserContext(FurmsViewUserContext furmsViewUserContext, boolean redirectable) {
		this(furmsViewUserContext.id, furmsViewUserContext.name, furmsViewUserContext.route,
				furmsViewUserContext.viewMode, redirectable);
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
