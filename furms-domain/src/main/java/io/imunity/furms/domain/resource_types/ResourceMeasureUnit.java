/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import java.util.Arrays;
import java.util.EnumSet;

public enum ResourceMeasureUnit {
	S("s", 0), MIN("min", 1), H("h", 2), DAY("day", 3),
	NONE("", 4), KILO("kilo", 5), MEGA("mega", 6), GIGA("giga", 7), TERA("tera", 8), PETA("peta", 9),
	KB("kB", 10), MB("MB", 11), GB("GB", 12), TB("TB", 13), PB("PB", 14);

	static final EnumSet<ResourceMeasureUnit> SI_UNIT = EnumSet.of(S, MIN, H, DAY);
	static final EnumSet<ResourceMeasureUnit> TIME_UNIT = EnumSet.of(NONE, KILO, MEGA, GIGA, TERA, PETA);
	static final EnumSet<ResourceMeasureUnit> DATA_UNIT = EnumSet.of(KB, MB, GB, TB, PB);

	private final String suffix;
	private final int persistentId;

	ResourceMeasureUnit(String suffix, int persistentId) {
		this.suffix = suffix;
		this.persistentId = persistentId;
	}

	public String getSuffix() {
		return suffix;
	}

	public int getPersistentId() {
		return persistentId;
	}

	public static ResourceMeasureUnit valueOf(int status){
		return Arrays.stream(values())
			.filter(userRemovalStatus -> userRemovalStatus.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));
	}
}
