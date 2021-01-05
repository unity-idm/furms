/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.tokens;

public interface AccessTokenRevoker {
	void revoke(String accessToken, String clientId);
}
