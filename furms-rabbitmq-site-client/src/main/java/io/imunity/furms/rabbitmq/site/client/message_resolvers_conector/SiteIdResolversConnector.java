/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_resolvers_conector;

import io.imunity.furms.rabbitmq.site.models.Body;

import java.util.Set;

public interface SiteIdResolversConnector extends SiteIdGetter {
	Set<Class<? extends Body>> getApplicableClasses();
}
