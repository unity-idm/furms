/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import java.util.Arrays;
import java.util.stream.Stream;

public interface ResourceMeasureUnit {

	String name();

	enum TimeUnit implements ResourceMeasureUnit {
		s, min, h, day
	}

	enum SiUnit implements ResourceMeasureUnit {
		kilo, mega, giga, tera, peta
	}

	enum DataUnit implements ResourceMeasureUnit {
		kB, MB, GB, TB, PB
	}

	static ResourceMeasureUnit valueOf(String s){
		return Stream.of(DataUnit.class, SiUnit.class, TimeUnit.class)
			.map(Class::getEnumConstants)
			.flatMap(Arrays::stream)
			.filter(e -> e.name().equals(s))
			.findAny()
			.orElse(null);
	}
}
