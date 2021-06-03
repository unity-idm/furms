/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import java.util.EnumSet;
import java.util.Set;

import static io.imunity.furms.domain.resource_types.ResourceMeasureUnit.*;
import static java.util.Collections.unmodifiableSet;

public enum ResourceMeasureType {
	INTEGER(toOrderedUnmodifiableSet(SI_UNIT)),
	FLOATING_POINT(toOrderedUnmodifiableSet(SI_UNIT)),
	TIME(toOrderedUnmodifiableSet(TIME_UNIT)),
	DATA(toOrderedUnmodifiableSet(DATA_UNIT));

	private static Set<ResourceMeasureUnit> toOrderedUnmodifiableSet(EnumSet<ResourceMeasureUnit> enumSet) {
		return unmodifiableSet(enumSet);
	}

	public final Set<ResourceMeasureUnit> units;

	ResourceMeasureType(Set<ResourceMeasureUnit> units) {
		this.units = units;
	}
}
