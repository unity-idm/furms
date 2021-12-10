/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import org.springframework.security.access.AccessDeniedException;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

public class SecurityTestRulesValidator {
	private Runnable[] methods;
	private TestUser[] accessGranted = new TestUser[0];
	private TestUser[] accessDenied = new TestUser[0];

	private SecurityTestRulesValidator(Runnable[] methods) {
		this.methods = methods;
	}

	public static SecurityTestRulesValidator forMethods(Runnable... methods) {
		return new SecurityTestRulesValidator(methods);
	}

	public SecurityTestRulesValidator accessFor(TestUser... users) {
		this.accessGranted = users;
		return this;
	}

	public SecurityTestRulesValidator deniedFor(TestUser... users) {
		this.accessDenied = users;
		return this;
	}

	public void validate(WireMockServer wireMockServer) {
		final Set<String> accessGrantedErrors = Arrays.stream(accessGranted)
				.map(user -> assertAccess(true, methods, user, wireMockServer))
				.filter(Objects::nonNull)
				.collect(toSet());
		final Set<String> accessDeniedErrors = Arrays.stream(accessDenied)
				.map(user -> assertAccess(false, methods, user, wireMockServer))
				.filter(Objects::nonNull)
				.collect(toSet());
		if (!accessGrantedErrors.isEmpty() || !accessDeniedErrors.isEmpty()) {
			throw new AssertionError("Security vulnerabilities:\n" +
					(!accessGrantedErrors.isEmpty() ? "Expected access granted for: \n "+accessGrantedErrors+" \n" : "") +
					(!accessDeniedErrors.isEmpty() ? "Expected access denied for: \n "+accessDeniedErrors+" \n" : ""));
		}
	}

	private String assertAccess(boolean isGranted, Runnable[] methods, TestUser user, WireMockServer wireMockServer) {
		try {
			user.registerUserMock(wireMockServer);
			user.registerInSecurityContext();

			final Set<Integer> methodIndexesWithWrongSecurity = IntStream.range(0, methods.length)
					.filter(index -> isGranted != isAccessGrantedTo(methods[index]))
					.boxed()
					.collect(toSet());

			if (!methodIndexesWithWrongSecurity.isEmpty()) {
				return format(" Methods with these indexes: %s should have%s been accessible for user with %s\n",
						methodIndexesWithWrongSecurity, isGranted ? "" : " not", user.getRoles());
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			user.removeFromSecurityContext();
		}
		return null;
	}

	private boolean isAccessGrantedTo(Runnable method) {
		try {
			method.run();
			return true;
		} catch (AccessDeniedException e) {
			return false;
		} catch (Exception e) {
			//ignore other exceptions
			return true;
		}
	}
}
