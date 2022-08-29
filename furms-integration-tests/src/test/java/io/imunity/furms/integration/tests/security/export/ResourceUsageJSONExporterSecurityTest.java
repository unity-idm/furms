/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.export;

import io.imunity.furms.api.export.ResourceUsageJSONExporter;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.imunity.furms.integration.tests.security.SecurityTestRulesValidator.forMethods;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.communityAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.fenixAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteSupport;

class ResourceUsageJSONExporterSecurityTest extends SecurityTestsBase {

	@Autowired
	private ResourceUsageJSONExporter service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(ResourceUsageJSONExporter.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInResourceUsageCSVExporter() {
		forMethods(
				() -> service.getJsonForCommunityAllocation(community, communityAllocation))
				.accessFor(
						fenixAdmin(),
						communityAdmin(community)
				)
				.deniedFor(
						basicUser(),
						siteAdmin(site),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(otherCommunity),
						projectAdmin(community, project),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
		.andForMethods(
				() -> service.getJsonForProjectAllocation(project, projectAllocation))
				.accessFor(
					basicUser(),
					fenixAdmin(),
					siteAdmin(site),
					siteAdmin(otherSite),
					siteSupport(site),
					siteSupport(otherSite),
					communityAdmin(community),
					communityAdmin(otherCommunity),
					projectAdmin(community, project),
					projectAdmin(otherCommunity, otherProject),
					projectUser(community, project),
					projectUser(otherCommunity, otherProject))
		.verifySecurityRulesAndInterfaceCoverage(ResourceUsageJSONExporter.class, server);
	}

}