/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.authz.token;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;

public class TokenRefreshResponse {

	private final String tokenValue;
	private final Instant issuedAt;
	private final Instant expiresAt;
	private final Set<String> scopes;

	public TokenRefreshResponse(LinkedHashMap<String, Object> response) {
		this.tokenValue = buildToken(response);
		this.issuedAt = Instant.now();
		this.expiresAt = buildExpiresIn(response);
		this.scopes = buildScopes(response);
	}

	private static String buildToken(LinkedHashMap<String, Object> response) {
		Object accessToken = response.get("access_token");
		if (accessToken == null) {
			throw new IllegalArgumentException("Access token is empty.");
		}
		return accessToken.toString();
	}

	private static Instant buildExpiresIn(LinkedHashMap<String, Object> response) {
		Object expiresIn = response.get("expires_in");
		return expiresIn == null
				? null
				: Instant.now().plusSeconds((Integer)expiresIn);
	}

	private static Set<String> buildScopes(LinkedHashMap<String, Object> response) {
		Object scopes = response.get("scopes");
		return scopes == null
				? Collections.emptySet()
				: Set.of(scopes.toString().split(" "));
	}

	public String getTokenValue() {
		return tokenValue;
	}

	public Instant getIssuedAt() {
		return issuedAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Set<String> getScopes() {
		return scopes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TokenRefreshResponse response = (TokenRefreshResponse) o;
		return Objects.equals(tokenValue, response.tokenValue) &&
				Objects.equals(issuedAt, response.issuedAt) &&
				Objects.equals(expiresAt, response.expiresAt) &&
				Objects.equals(scopes, response.scopes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tokenValue, issuedAt, expiresAt, scopes);
	}

	@Override
	public String toString() {
		return "TokenRefreshResponse{" +
				"tokenValue='" + tokenValue + '\'' +
				", issuedAt=" + issuedAt +
				", expiresAt=" + expiresAt +
				", scopes=" + scopes +
				'}';
	}
}
