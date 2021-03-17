/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import io.imunity.furms.domain.resource_types.ResourceMeasureUnit.SiUnit;

import java.util.Set;

import static io.imunity.furms.domain.resource_types.ResourceMeasureUnit.DataUnit;
import static io.imunity.furms.domain.resource_types.ResourceMeasureUnit.TimeUnit;

public enum ResourceMeasureType {
	INTEGER(Set.of(SiUnit.values())),
	FLOATING_POINT(Set.of(SiUnit.values())),
	TIME(Set.of(TimeUnit.values())),
	DATA(Set.of(DataUnit.values()));

	public final Set<ResourceMeasureUnit> units;

	ResourceMeasureType(Set<ResourceMeasureUnit> units) {
		this.units = units;
	}
}
