/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config;

import java.util.List;

@FunctionalInterface
public interface CapabilitiesGetter {
	List<String> getCapabilities();
}