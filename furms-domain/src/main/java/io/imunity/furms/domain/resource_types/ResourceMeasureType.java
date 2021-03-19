/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import io.imunity.furms.domain.resource_types.ResourceMeasureUnit.SiUnit;

import java.util.LinkedHashSet;
import java.util.Set;

import static io.imunity.furms.domain.resource_types.ResourceMeasureUnit.DataUnit;
import static io.imunity.furms.domain.resource_types.ResourceMeasureUnit.TimeUnit;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public enum ResourceMeasureType {
	INTEGER(toOrderedUnmodifiableSet(SiUnit.values())),
	FLOATING_POINT(toOrderedUnmodifiableSet(SiUnit.values())),
	TIME(toOrderedUnmodifiableSet(TimeUnit.values())),
	DATA(toOrderedUnmodifiableSet(DataUnit.values()));

	private static Set<ResourceMeasureUnit> toOrderedUnmodifiableSet(ResourceMeasureUnit[] array) {
		return unmodifiableSet(new LinkedHashSet<>(asList(array)));
	}

	public final Set<ResourceMeasureUnit> units;

	ResourceMeasureType(Set<ResourceMeasureUnit> units) {
		this.units = units;
	}
}
