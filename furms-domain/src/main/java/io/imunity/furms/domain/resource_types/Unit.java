/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import java.util.Arrays;
import java.util.stream.Stream;

public interface Unit {

	String name();

	enum TimeUnit implements Unit{
		s, min, h, day
	}

	enum SiUnit implements Unit{
		s, m, kg, mol, cd, K, A
	}

	enum DataUnit implements Unit{
		kB, MB, GB, TB, PB
	}

	static Unit valueOf(String s){
		return Stream.of(DataUnit.class, SiUnit.class, TimeUnit.class)
			.map(Class::getEnumConstants)
			.flatMap(Arrays::stream)
			.filter(e -> e.name().equals(s))
			.findAny()
			.orElse(null);
	}
}
