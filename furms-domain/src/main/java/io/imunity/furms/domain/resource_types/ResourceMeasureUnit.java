/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import java.util.Arrays;
import java.util.EnumSet;

public enum ResourceMeasureUnit {
	S("s", "s"), MIN("min", "min"), H("h", "h"), DAY("day", "day"),
	NONE("", "none"), KILO("kilo", "kilo"), MEGA("mega", "mega"), GIGA("giga", "giga"), TERA("tera", "tera"), PETA("peta", "peta"),
	KB("kB", "kB"), MB("MB", "MB"), GB("GB", "GB"), TB("TB", "TB"), PB("PB", "PB");

	static final EnumSet<ResourceMeasureUnit> TIME_UNIT = EnumSet.of(S, MIN, H, DAY);
	static final EnumSet<ResourceMeasureUnit> SI_UNIT = EnumSet.of(NONE, KILO, MEGA, GIGA, TERA, PETA);
	static final EnumSet<ResourceMeasureUnit> DATA_UNIT = EnumSet.of(KB, MB, GB, TB, PB);

	private final String suffix;
	private final String persistentId;

	ResourceMeasureUnit(String suffix, String persistentId) {
		this.suffix = suffix;
		this.persistentId = persistentId;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getPersistentId() {
		return persistentId;
	}

	public static ResourceMeasureUnit fromCode(String code){
		return Arrays.stream(values())
			.filter(userRemovalStatus -> userRemovalStatus.getPersistentId().equals(code))
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad code - %s, it shouldn't happen", code)));
	}
}
