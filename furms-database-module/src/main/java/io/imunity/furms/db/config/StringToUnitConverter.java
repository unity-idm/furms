/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.config;

import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
class StringToUnitConverter implements Converter<String, ResourceMeasureUnit> {
	@Override
	public ResourceMeasureUnit convert(String s) {
		return ResourceMeasureUnit.valueOf(s);
	}
}
