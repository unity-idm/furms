/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.layout;

import io.imunity.furms.ui.components.FurmsViewComponent;

import java.util.Objects;
import java.util.Optional;

class BreadCrumb {
	private final Class<? extends FurmsViewComponent> routeClass;
	private final BreadCrumbParameter breadCrumbParameter;

	BreadCrumb(Class<? extends FurmsViewComponent> routeClass, BreadCrumbParameter breadCrumbParameter) {
		this.routeClass = routeClass;
		this.breadCrumbParameter = breadCrumbParameter;
	}

	public Class<? extends FurmsViewComponent> getRouteClass() {
		return routeClass;
	}

	public Optional<BreadCrumbParameter> getBreadCrumbParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}

	public boolean isParamChanged(BreadCrumb newParameter){
		return breadCrumbParameter != null && breadCrumbParameter.id != null && breadCrumbParameter.id.equals(newParameter.breadCrumbParameter.id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BreadCrumb that = (BreadCrumb) o;
		return Objects.equals(routeClass, that.routeClass) &&
			Objects.equals(breadCrumbParameter, that.breadCrumbParameter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(routeClass, breadCrumbParameter);
	}

	@Override
	public String toString() {
		return "BreadCrumb{" +
			"routeClass=" + routeClass +
			", breadCrumbParameter=" + breadCrumbParameter +
			'}';
	}
}
