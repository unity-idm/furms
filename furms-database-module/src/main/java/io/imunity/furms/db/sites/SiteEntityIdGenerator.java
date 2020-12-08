/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Stream;

import static io.imunity.furms.db.sites.SiteEntityUtils.generateId;

@Configuration
class SiteEntityIdGenerator {

	@Bean
	ApplicationListener<BeforeSaveEvent> idGenerator() {
		return event -> {
			if (event.getEntity() instanceof SiteEntity) {
				SiteEntity siteEntity = (SiteEntity) event.getEntity();
				if (siteEntity.getId() == null) {
					try {
						Optional<Field> id = Stream.of(SiteEntity.class.getDeclaredFields())
								.filter(field -> field.isAnnotationPresent(Id.class))
								.findFirst();
						if (id.isEmpty()) {
							throw new NoSuchFieldException("Could not find ID field for Site Entity");
						}
						id.get().setAccessible(true);
						ReflectionUtils.setField(id.get(), siteEntity, generateId());
					} catch (NoSuchFieldException e) {
						throw new UnsupportedOperationException(e.getMessage());
					}
				}
			}
		};
	}
}
