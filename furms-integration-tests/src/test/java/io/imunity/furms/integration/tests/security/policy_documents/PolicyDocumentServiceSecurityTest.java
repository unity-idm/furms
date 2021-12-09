/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.policy_documents;

import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PolicyDocumentServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private PolicyDocumentService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(PolicyDocumentService.class, service);
	}

	@Test
	void userWith_SITE_READ_canFindById() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findById(site, policy));
	}

	@Test
	void userWithoutResourceSpecified_SITE_READ_canFindAll() throws Throwable {
		assertsForUserWith_SITE_READ_withoutResourceSpecified(() -> service.findAll());
	}

	@Test
	void userWith_SITE_READ_canFindAllUsersPolicies() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAllUsersPolicies(site));
	}

	@Test
	void userWith_SITE_READ_canFindAllBySiteId() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAllBySiteId(site));
	}

	@Test
	void userWith_SITE_POLICY_ACCEPTANCE_READ_canFindAllUsersPolicyAcceptances() throws Throwable {
		assertsForUserWith_SITE_POLICY_ACCEPTANCE_READ(() -> service.findAllUsersPolicyAcceptances(site));
	}

	@Test
	void userWith_SITE_POLICY_ACCEPTANCE_READ_canFindAllUsersPolicyAcceptancesByPolicyId() throws Throwable {
		assertsForUserWith_SITE_POLICY_ACCEPTANCE_READ(() -> service.findAllUsersPolicyAcceptances(policy, site));
	}

	@Test
	void userWith_AUTHENTICATED_canFindAllByCurrentUser() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.findAllByCurrentUser());
	}

	@Test
	void userWith_SITE_POLICY_ACCEPTANCE_READ_canResendPolicyInfo() throws Throwable {
		assertsForUserWith_SITE_POLICY_ACCEPTANCE_READ(() -> service.resendPolicyInfo(site, new PersistentId("id"), policy));
	}

	@Test
	void userWith_AUTHENTICATED_canAddCurrentUserPolicyAcceptance() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.addCurrentUserPolicyAcceptance(PolicyAcceptance.builder().build()));
	}

	@Test
	void userWith_SITE_POLICY_ACCEPTANCE_READ_canAddUserPolicyAcceptance() throws Throwable {
		assertsForUserWith_SITE_POLICY_ACCEPTANCE_READ(() -> service.addUserPolicyAcceptance(site, new FenixUserId("id"),
				PolicyAcceptance.builder().build()));
	}

	@Test
	void userWith_SITE_WRITE_canCreate() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.create(PolicyDocument.builder().siteId(site).build()));
	}

	@Test
	void userWith_SITE_WRITE_canUpdate() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.update(PolicyDocument.builder().siteId(site).build()));
	}

	@Test
	void userWith_SITE_WRITE_canUpdateWithRevision() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.updateWithRevision(PolicyDocument.builder().siteId(site).build()));
	}

	@Test
	void userWith_SITE_WRITE_canDelete() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.delete(site, policy));
	}

}