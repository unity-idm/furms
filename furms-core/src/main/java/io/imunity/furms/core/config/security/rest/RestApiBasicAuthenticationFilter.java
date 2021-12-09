/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.rest;

import io.imunity.furms.api.user.api.key.UserApiKeyService;
import io.imunity.furms.core.users.api.key.AdminApiKeyFinder;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.roles.RoleLoader;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class RestApiBasicAuthenticationFilter extends BasicAuthenticationFilter {

	private final static String REST_API_CIDP_CALLS_PATTERN = "\\/rest-api\\/.*\\/cidp\\/.*";

	private final AdminApiKeyFinder adminApiKeyFinder;
	private final RoleLoader roleLoader;
	private final BasicAuthenticationConverter authenticationConverter;

	public RestApiBasicAuthenticationFilter(
			AuthenticationManager authenticationManager,
			AdminApiKeyFinder adminApiKeyFinder,
			RoleLoader roleLoader
	) {
		super(authenticationManager);
		this.adminApiKeyFinder = adminApiKeyFinder;
		this.roleLoader = roleLoader;
		this.authenticationConverter = new BasicAuthenticationConverter();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			if (!request.getRequestURI().matches(REST_API_CIDP_CALLS_PATTERN)) {
				final UsernamePasswordAuthenticationToken authRequest = authenticationConverter.convert(request);
				if (authRequest == null) {
					chain.doFilter(request, response);
					return;
				}

				final PersistentId userId = new PersistentId(authRequest.getPrincipal().toString());
				final UUID apiKey = UUID.fromString(authRequest.getCredentials().toString());

				final Optional<FURMSUser> user = adminApiKeyFinder.findUserByUserIdAndApiKey(userId, apiKey);
				if (user.isEmpty()) {
					chain.doFilter(request, response);
					return;
				}

				final FURMSUser userWithRoles = new FURMSUser(user.get(), roleLoader.loadUserRoles(userId));
				final Authentication authResult = new UsernamePasswordAuthenticationToken(
						new RestApiUser(userId.id, apiKey.toString(), userWithRoles),
						null, Set.of());

				SecurityContextHolder.getContext().setAuthentication(authResult);
				this.onSuccessfulAuthentication(request, response, authResult);

				chain.doFilter(request, response);
			} else {
				super.doFilterInternal(request, response, chain);
			}
		} catch (Exception e) {
			super.doFilterInternal(request, response, chain);
		}
	}
}
