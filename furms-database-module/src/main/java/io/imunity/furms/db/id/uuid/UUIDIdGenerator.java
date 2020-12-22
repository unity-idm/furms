/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.id.uuid;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;

@Configuration
class UUIDIdGenerator {

	@Bean
	ApplicationListener<BeforeSaveEvent<?>> idGenerator() {
		return event -> {
			if (event.getEntity() instanceof UUIDIdentifiable) {
				UUIDIdentifiable entity = (UUIDIdentifiable) event.getEntity();
				if (entity.getId() == null) {
					entity.setId(generateId());
				}
			}
		};
	}
}
