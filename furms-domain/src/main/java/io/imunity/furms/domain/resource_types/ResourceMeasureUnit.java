/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import java.util.Arrays;
import java.util.stream.Stream;

public interface ResourceMeasureUnit {

	String getSuffix();
	String getName();

	enum TimeUnit implements ResourceMeasureUnit {
		s("s"), min("min"), h("h"), day("day");
		
		private final String suffix;

		private TimeUnit(String suffix) {
			this.suffix = suffix;
		}

		@Override
		public String getSuffix() {
			return suffix;
		}

		@Override
		public String getName()	{
			return name();
		}
	}

	enum SiUnit implements ResourceMeasureUnit {
		none(""), kilo("kilo"), mega("mega"), giga("giga"), tera("tera"), peta("peta");

		private final String suffix;

		private SiUnit(String suffix) {
			this.suffix = suffix;
		}

		@Override
		public String getSuffix() {
			return suffix;
		}

		@Override
		public String getName()	{
			return name();
		}
	}

	enum DataUnit implements ResourceMeasureUnit {
		kB("kB"), MB("MB"), GB("GB"), TB("TB"), PB("PB");

		private final String suffix;

		private DataUnit(String suffix) {
			this.suffix = suffix;
		}

		@Override
		public String getSuffix() {
			return suffix;
		}

		@Override
		public String getName()	{
			return name();
		}
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
