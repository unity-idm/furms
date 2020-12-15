/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user;

import java.util.List;

public interface FurmsRole {
	List<Capability> getCapabilities();
	String name();
}