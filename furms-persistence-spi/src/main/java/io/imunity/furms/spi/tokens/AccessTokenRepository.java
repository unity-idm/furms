/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.tokens;

import io.imunity.furms.domain.authz.token.TokenRefreshResponse;

public interface AccessTokenRepository {
	void revoke(String accessToken, String clientId);
	TokenRefreshResponse refresh(String refreshToken, String clientId) throws Exception;
}
