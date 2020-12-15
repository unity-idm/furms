/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user;

import java.util.Objects;
import java.util.Optional;

public class ViewContext {
	public final ViewMode viewMode;
	public final Optional<ResourceId> resourceId;

	public ViewContext(ViewMode viewMode) {
		this.viewMode = viewMode;
		this.resourceId = Optional.empty();
	}

	public ViewContext(ViewMode viewMode, ResourceId resourceId) {
		this.viewMode = viewMode;
		this.resourceId = Optional.ofNullable(resourceId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ViewContext that = (ViewContext) o;
		return viewMode == that.viewMode &&
			Objects.equals(resourceId, that.resourceId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(viewMode, resourceId);
	}

	@Override
	public String toString() {
		return "ViewContext{" +
			"viewMode=" + viewMode +
			", resourceId=" + resourceId +
			'}';
	}
}