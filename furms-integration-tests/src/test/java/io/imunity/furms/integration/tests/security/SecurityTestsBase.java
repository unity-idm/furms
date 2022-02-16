/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.config.security.method.FurmsPublicAccess;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunity;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunityAllocation;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultPolicy;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProject;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProjectAllocation;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceCredit;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceType;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultService;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultSite;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class SecurityTestsBase extends IntegrationTestBase {

	protected String site;
	protected String otherSite = UUID.randomUUID().toString();
	protected PolicyId policy;
	protected String resourceCredit;
	protected String resourceType;
	protected String infraService;
	protected String community;
	protected String otherCommunity = UUID.randomUUID().toString();
	protected String communityAllocation;
	protected String project;
	protected String otherProject = UUID.randomUUID().toString();
	protected String projectAllocation;
	protected PersistentId persistentId = new PersistentId("id");
	protected FenixUserId fenixId = new FenixUserId("id");

	@BeforeEach
	protected void setUp() {
		site = siteRepository.create(defaultSite().name("site1").build(), new SiteExternalId("se_id"));
		policy = policyDocumentRepository.create(defaultPolicy()
				.siteId(site)
				.build());
		final String serviceName = UUID.randomUUID().toString();
		infraService = infraServiceRepository.create(defaultService()
				.siteId(site)
				.name(serviceName)
				.policyId(policy)
				.build());
		resourceType = resourceTypeRepository.create(defaultResourceType()
				.siteId(site)
				.serviceId(infraService)
				.name(UUID.randomUUID().toString())
				.build());
		resourceCredit = resourceCreditRepository.create(defaultResourceCredit()
				.siteId(site)
				.resourceTypeId(resourceType)
				.name("RC 1")
				.amount(BigDecimal.TEN)
				.build());
		community = communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());;
		communityAllocation = communityAllocationRepository.create(defaultCommunityAllocation()
				.communityId(community)
				.resourceCreditId(resourceCredit)
				.name(UUID.randomUUID().toString())
				.amount(BigDecimal.TEN)
				.build());
		project = projectRepository.create(defaultProject()
				.communityId(community)
				.name(UUID.randomUUID().toString())
				.leaderId(new PersistentId(ADMIN_USER.getUserId()))
				.build());
		projectAllocation = projectAllocationRepository.create(defaultProjectAllocation()
				.communityAllocationId(communityAllocation)
				.projectId(project)
				.name(UUID.randomUUID().toString())
				.amount(BigDecimal.ONE)
				.build());

		//disable annoying logs for unmatched wiremock stubs
		((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("wiremock").setLevel(Level.OFF);
		((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("WireMock").setLevel(Level.OFF);
	}

	@AfterEach
	protected void cleanLoggingSettings() {
		//return control to Spring about Wiremock logging
		((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("wiremock").setLevel(null);
		((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("WireMock").setLevel(null);
	}

	protected void assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(final Class<?> baseInterface, final Object implementation) {
		final Method[] implementedMethods = (AopUtils.isAopProxy(implementation) || AopUtils.isCglibProxy(implementation))
				? implementation.getClass().getSuperclass().getMethods()
				: implementation.getClass().getMethods();

		final Set<Method> interfaceImplementedMethods = Stream.of(implementedMethods)
				.filter(method -> Arrays.stream(baseInterface.getMethods())
						.anyMatch(baseMethod ->
								baseMethod.getName().equals(method.getName())
										&& baseMethod.getReturnType().equals(method.getReturnType())
										&& Arrays.equals(baseMethod.getParameterTypes(), method.getParameterTypes())))
				.collect(toSet());

		if (baseInterface.getMethods().length > 0) {
			assertThat(interfaceImplementedMethods).hasSize(baseInterface.getMethods().length);
		}

		final Set<String> unsecuredMethods = interfaceImplementedMethods.stream()
				.filter(method -> !method.isAnnotationPresent(FurmsAuthorize.class) && !method.isAnnotationPresent(FurmsPublicAccess.class))
				.map(Method::getName)
				.collect(toSet());

		if (!unsecuredMethods.isEmpty()) {
			throw new AssertionError(format("These methods in interface %s are expected to be secured: \n %s", baseInterface, unsecuredMethods));
		}
	}

}
