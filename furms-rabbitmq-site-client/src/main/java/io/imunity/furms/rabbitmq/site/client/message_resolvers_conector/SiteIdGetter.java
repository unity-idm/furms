/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_resolvers_conector;

import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.Payload;

import java.util.Optional;

public interface SiteIdGetter {
	Optional<SiteExternalId> getSiteId(Payload<?> payload);
}
