/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user.role;

import io.imunity.furms.core.config.security.user.capability.Capability;

import java.util.List;

public interface SpecialRole {
	List<Capability> getAdditionalCapabilities();
}