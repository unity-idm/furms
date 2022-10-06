/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain;

import java.util.UUID;

public interface Id {
	String asRawString();
	
	
	class RawIdParser {
		public static String asRawString(UUID id)
		{
			return id == null ? null : id.toString();
		}
	}
}
