/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

public class FurmsViewUserContext implements Comparable<FurmsViewUserContext>{
	
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public final String id;
	public final String name;
	public final String route;
	public final ViewMode viewMode;

	private FurmsViewUserContext(String id, String name, String route, ViewMode viewMode) {
		Preconditions.checkNotNull(id);
		this.id = id;
		this.name = name;
		this.route = route;
		this.viewMode = viewMode;
	}

	FurmsViewUserContext(String id, String name, ViewMode viewMode) {
		this(id, name, viewMode.route, viewMode);
	}

	FurmsViewUserContext(String id, String name, ViewMode viewMode, String route) {
		this(id, name, route, viewMode);
	}

	public void setAsCurrent() {
		UI ui = UI.getCurrent();
		ComponentUtil.setData(ui, FurmsViewUserContext.class, this);
		LOG.debug("Set current furms user context: {}", this);
	}

	public static FurmsViewUserContext getCurrent() {
		UI ui = UI.getCurrent();
		if (ui == null)
			throw new IllegalStateException("No UI set when trying to obtain FurmsViewUserContext");
		return ComponentUtil.getData(ui, FurmsViewUserContext.class);
	}

	@Override
	public int compareTo(FurmsViewUserContext o)
	{
		return name.compareToIgnoreCase(o.name);
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
