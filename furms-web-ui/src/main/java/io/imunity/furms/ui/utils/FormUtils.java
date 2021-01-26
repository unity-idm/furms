/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.binder.Binder;
import io.imunity.furms.ui.views.site.settings.SiteSettingsDto;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class FormUtils {

	public static <T extends Component> Optional<T> findFormField(Binder<SiteSettingsDto> binder, Class<T> fieldType, String id) {
		List<T> fields = binder.getFields()
				.filter(fieldType::isInstance)
				.map(field -> (T) field)
				.filter(field -> field.getId()
						.map(id::equals)
						.orElse(false))
				.collect(toList());
		if (fields.size() > 1) {
			throw new IllegalArgumentException(format("Registered binder has more that one field for class: %s and id: %s",
					fieldType.getName(), id));
		}
		return fields.stream().findAny();
	}

}
