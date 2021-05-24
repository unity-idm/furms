/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.core.config.security;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.imunity.furms.api.authz.FURMSUserProvider;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.utils.MDCKey;

/**
 * Used to set MDC context of user initiating action from UI and REST. 
 * Note that not all of the REST users has persistent identifier defined.
 */
public class UserContextSetterFilter extends OncePerRequestFilter {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		SecurityContext context = SecurityContextHolder.getContext();
		String userId = getUserMDC(context);

		MDC.put(MDCKey.USER_ID.key, userId);
		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(MDCKey.USER_ID.key);
		}
	}

	private String getUserMDC(SecurityContext context) {
		String userId = "";
		if (context != null && context.getAuthentication() != null
				&& context.getAuthentication().getPrincipal() != null) {
			try {
				FURMSUserProvider user = (FURMSUserProvider) context.getAuthentication().getPrincipal();
				final FURMSUser furmsUser = user.getFURMSUser();
				userId = furmsUser.id.map(persistentId -> persistentId.id).orElseGet(() -> furmsUser.email);
			} catch (Exception ex) {
				LOG.error("Failed to retrieve user id from security context", ex);
			}
		}
		return userId;
	}
}
