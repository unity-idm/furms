/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.config;

import io.imunity.furms.domain.resource_types.Unit;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
class UnitToStringConverter implements Converter<Unit, String> {

	@Override
	public String convert(Unit unit) {
		return unit.toString();
	}
}
