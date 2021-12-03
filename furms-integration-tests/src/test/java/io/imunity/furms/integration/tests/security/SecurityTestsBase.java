/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.config.security.method.FurmsPublicAccess;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;

import static ch.qos.logback.classic.Level.INFO;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunity;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunityAllocation;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultPolicy;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProject;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProjectAllocation;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceCredit;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceType;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultService;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultSite;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.communityAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.fenixAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteSupport;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SecurityTestsBase extends IntegrationTestBase {

	protected String site;
	protected PolicyId policy;
	protected String resourceCredit;
	protected String resourceType;
	protected String infraService;
	protected String community;
	protected String communityAllocation;
	protected String project;
	protected String projectAllocation;

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
	protected void tearDown() {
		//return control to Spring about Wiremock logging
		((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("wiremock").setLevel(null);
		((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("WireMock").setLevel(null);
	}

	protected void assertThatAllPublicMethodsBeAnnotatedWithSecurityAnnotation(final Class<?> serviceType) {
		final Method[] methods = isInjectedImplementations(serviceType)
				? serviceType.getSuperclass().getDeclaredMethods()
				: serviceType.getDeclaredMethods();
		Stream.of(methods)
				.filter(method -> Modifier.isPublic(method.getModifiers()))
				.forEach(method ->
					assertThat(method.isAnnotationPresent(FurmsAuthorize.class)
							|| method.isAnnotationPresent(FurmsPublicAccess.class)).isTrue());
	}

	protected void assertThatUserHasAccessToCallMethod(final Runnable method, final TestUser user) {
		try {
			setupUser(user);
			user.registerInSecurityContext();

			method.run();
		} catch (AccessDeniedException e) {
			throw new AssertionError(e.getClass().getName() + " has been thrown but It wasn't expected.");
		} catch (Exception e) {
			//ignore other exceptions
		} finally {
			user.removeFromSecurityContext();
		}
	}

	protected void assertThatAccessDeniedExceptionShouldBeThrownWithUser(final Runnable method, final TestUser user) throws Throwable {
		setupUser(user);
		user.registerInSecurityContext();

		assertThrows(AccessDeniedException.class, method::run);
		user.removeFromSecurityContext();
	}

	protected void assertThatThisMethodCanBeCalledWithoutCapabilities(final Runnable method) {
		try {
			SecurityContextHolder.getContext().setAuthentication(null);
			method.run();
		} catch (AccessDeniedException e) {
			throw new AssertionError(e.getClass().getName() + " has been thrown but It wasn't expected.");
		} catch (Exception e) {
			//ignore other exceptions
		}
	}

	protected void assertsForUserWith_USERS_MAINTENANCE(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
	}

	protected void assertsForUserWith_REST_API_KEY_MANAGEMENT_withoutResourceSpecified(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());
		assertThatUserHasAccessToCallMethod(method, siteAdmin(UUID.randomUUID().toString()));
		assertThatUserHasAccessToCallMethod(method, communityAdmin(UUID.randomUUID().toString()));
		assertThatUserHasAccessToCallMethod(method, projectAdmin(UUID.randomUUID().toString(), UUID.randomUUID().toString()));

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
	}

	protected void assertsForUserWith_AUTHENTICATED(final Runnable method) {
		assertThatUserHasAccessToCallMethod(method, basicUser());
	}

	protected void assertsForUserWith_SITE_READ(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());
		assertThatUserHasAccessToCallMethod(method, siteSupport(site));
		assertThatUserHasAccessToCallMethod(method, siteAdmin(site));

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, siteAdmin(UUID.randomUUID().toString()));
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, siteSupport(UUID.randomUUID().toString()));
	}

	protected void assertsForUserWith_SITE_READ_withoutResourceSpecified(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, siteSupport(site));
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, siteAdmin(site));
	}

	protected void assertsForUserWith_SITE_WRITE(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());
		assertThatUserHasAccessToCallMethod(method, siteAdmin(site));

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, siteAdmin(UUID.randomUUID().toString()));
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, siteSupport(site));
	}

	protected void assertsForUserWith_SITE_WRITE_withoutResourceSpecified(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, siteSupport(site));
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, siteAdmin(site));
	}

	protected void assertsForUserWith_SITE_POLICY_ACCEPTANCE_READ(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());
		assertThatUserHasAccessToCallMethod(method, siteSupport(site));
		assertThatUserHasAccessToCallMethod(method, siteAdmin(site));

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, siteAdmin(UUID.randomUUID().toString()));
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, siteSupport(UUID.randomUUID().toString()));
	}

	protected void assertsForUserWith_COMMUNITY_READ(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());
		assertThatUserHasAccessToCallMethod(method, communityAdmin(community));

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, communityAdmin(UUID.randomUUID().toString()));
	}

	protected void assertsForUserWith_COMMUNITY_READ_withoutResourceSpecified(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, communityAdmin(community));
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, communityAdmin(UUID.randomUUID().toString()));
	}

	protected void assertsForUserWith_COMMUNITY_WRITE(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());
		assertThatUserHasAccessToCallMethod(method, communityAdmin(community));

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, communityAdmin(UUID.randomUUID().toString()));
	}

	protected void assertsForUserWith_COMMUNITY_WRITE_withoutResourceSpecified(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, communityAdmin(UUID.randomUUID().toString()));
	}

	protected void assertsForUserWith_MEMBERSHIP_GROUP_READ(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, communityAdmin(community));

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, communityAdmin(UUID.randomUUID().toString()));
	}

	protected void assertsForUserWith_MEMBERSHIP_GROUP_WRITE(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, communityAdmin(community));

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, communityAdmin(UUID.randomUUID().toString()));
	}

	protected void assertsForUserWith_FENIX_ADMINS_MANAGEMENT(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
	}

	protected void assertsForUserWith_PROJECT_READ(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, communityAdmin(community));
		assertThatUserHasAccessToCallMethod(method, projectAdmin(community, project));
		assertThatUserHasAccessToCallMethod(method, projectUser(community, project));

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
	}

	protected void assertsForUserWith_PROJECT_WRITE(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, communityAdmin(community));

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, communityAdmin(UUID.randomUUID().toString()));
	}

	protected void assertsForUserWith_PROJECT_LIMITED_READ(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());
		assertThatUserHasAccessToCallMethod(method, siteAdmin(site));
		assertThatUserHasAccessToCallMethod(method, communityAdmin(community));
		assertThatUserHasAccessToCallMethod(method, projectAdmin(community, project));
		assertThatUserHasAccessToCallMethod(method, projectUser(community, project));
	}

	protected void assertsForUserWith_PROJECT_LIMITED_WRITE(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, communityAdmin(community));
		assertThatUserHasAccessToCallMethod(method, projectAdmin(community, project));

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, fenixAdmin());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, siteAdmin(site));
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, siteSupport(site));
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, projectUser(community, project));
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, communityAdmin(UUID.randomUUID().toString()));
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, projectAdmin(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
	}

	protected void assertsForUserWith_PROJECT_ADMINS_MANAGEMENT(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, communityAdmin(community));
		assertThatUserHasAccessToCallMethod(method, projectAdmin(community, project));

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, communityAdmin(UUID.randomUUID().toString()));
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, projectAdmin(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
	}

	protected void assertsForUserWith_READ_ALL_USERS(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());
		assertThatUserHasAccessToCallMethod(method, siteAdmin(UUID.randomUUID().toString()));
		assertThatUserHasAccessToCallMethod(method, communityAdmin(UUID.randomUUID().toString()));
		assertThatUserHasAccessToCallMethod(method, projectAdmin(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
	}

	protected void assertsForUserWith_PROJECT_LEAVE(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, communityAdmin(community));
		assertThatUserHasAccessToCallMethod(method, projectAdmin(community, project));
		assertThatUserHasAccessToCallMethod(method, projectUser(community, project));

		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, basicUser());
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, communityAdmin(UUID.randomUUID().toString()));
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, projectAdmin(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		assertThatAccessDeniedExceptionShouldBeThrownWithUser(method, projectUser(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
	}

	protected void assertsForUserWith_OWNED_SSH_KEY_MANAGMENT_withoutResourceSpecified(final Runnable method) throws Throwable {
		assertThatUserHasAccessToCallMethod(method, fenixAdmin());
		assertThatUserHasAccessToCallMethod(method, siteAdmin(UUID.randomUUID().toString()));
		assertThatUserHasAccessToCallMethod(method, siteSupport(UUID.randomUUID().toString()));
		assertThatUserHasAccessToCallMethod(method, communityAdmin(UUID.randomUUID().toString()));
		assertThatUserHasAccessToCallMethod(method, projectAdmin(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		assertThatUserHasAccessToCallMethod(method, projectUser(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
	}

	private boolean isInjectedImplementations(Class<?> serviceType) {
		return serviceType.getName().contains("EnhancerBySpringCGLIB");
	}

}
