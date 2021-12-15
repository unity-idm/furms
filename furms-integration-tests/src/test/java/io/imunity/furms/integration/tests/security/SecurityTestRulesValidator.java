/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

public class SecurityTestRulesValidator {
	private RulesData currentRules;
	private List<RulesData> allRules;

	private SecurityTestRulesValidator(Runnable[] methods) {
		this.currentRules = new RulesData();
		this.allRules = new ArrayList<>();
		this.currentRules.methods = methods;
	}

	public static SecurityTestRulesValidator forMethods(Runnable... methods) {
		return new SecurityTestRulesValidator(methods);
	}

	public SecurityTestRulesValidator accessFor(TestUser... users) {
		this.currentRules.accessGranted = users;
		return this;
	}

	public SecurityTestRulesValidator deniedFor(TestUser... users) {
		this.currentRules.accessDenied = users;
		return this;
	}

	public SecurityTestRulesValidator andForMethods(Runnable... methods) {
		allRules.add(new RulesData(currentRules));
		currentRules = new RulesData();
		currentRules.methods = methods;

		return this;
	}

	public SecurityTestRulesValidator verifySecurityRulesAndInterfaceCoverage(Class<?> service, WireMockServer wireMockServer) {
		allRules.add(new RulesData(currentRules));
		currentRules = new RulesData();

		verifySecurityRules(wireMockServer);
		verifyInterfaceMethodsSecurityCoverage(service);
		return this;
	}

	private void verifySecurityRules(WireMockServer wireMockServer) {
		allRules.forEach(rule -> {
			final Set<String> accessGrantedErrors = Arrays.stream(rule.accessGranted)
					.map(user -> assertAccess(true, rule.methods, user, wireMockServer))
					.filter(Objects::nonNull)
					.collect(toSet());
			final Set<String> accessDeniedErrors = Arrays.stream(rule.accessDenied)
					.map(user -> assertAccess(false, rule.methods, user, wireMockServer))
					.filter(Objects::nonNull)
					.collect(toSet());
			if (!accessGrantedErrors.isEmpty() || !accessDeniedErrors.isEmpty()) {
				throw new AssertionError("Security vulnerabilities:\n" +
						(!accessGrantedErrors.isEmpty() ? "Expected access granted for: \n "+accessGrantedErrors+" \n" : "") +
						(!accessDeniedErrors.isEmpty() ? "Expected access denied for: \n "+accessDeniedErrors+" \n" : ""));
			}
		});
	}

	private void verifyInterfaceMethodsSecurityCoverage(Class<?> service) {
		final long testedMethodsCount = allRules.stream()
				.map(rule -> rule.methods)
				.flatMap(Arrays::stream)
				.count();
		if (service.getDeclaredMethods().length != testedMethodsCount) {
			throw new AssertionError(format("Not exactly all methods have been covered by this security tests. \n" +
					"Actual number of tested methods %s isn't meet expected %s", testedMethodsCount, service.getDeclaredMethods().length));
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

	private static class RulesData {
		private Runnable[] methods = new Runnable[0];
		private TestUser[] accessGranted = new TestUser[0];
		private TestUser[] accessDenied = new TestUser[0];

		public RulesData() {
		}

		public RulesData(RulesData currentRules) {
			this.methods = Arrays.copyOf(currentRules.methods, currentRules.methods.length);
			this.accessGranted = Arrays.copyOf(currentRules.accessGranted, currentRules.accessGranted.length);
			this.accessDenied = Arrays.copyOf(currentRules.accessDenied, currentRules.accessDenied.length);
		}
	}
}
