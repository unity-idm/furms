/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api;

import io.imunity.furms.domain.site_messages.PingStatus;

import java.util.concurrent.CompletableFuture;

public interface SiteMessager {
	CompletableFuture<PingStatus> ping();
}
