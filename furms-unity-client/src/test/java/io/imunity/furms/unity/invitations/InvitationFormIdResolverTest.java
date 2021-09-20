/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.invitations;

import io.imunity.furms.domain.authz.roles.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvitationFormIdResolverTest {

	private InvitationFormIdResolver invitationFormIdResolver;

	@BeforeEach
	void setUp() {
		invitationFormIdResolver = new InvitationFormIdResolver(
			"fenixFormId",
			"siteFormId",
			"communityFormId",
			"projectFormId"
		);
	}

	@Test
	void shouldResolveFormIdForFenixAdmin(){
		Role role = Role.FENIX_ADMIN;

		String formId = invitationFormIdResolver.getFormId(role);

		assertEquals("fenixFormId", formId);
	}

	@Test
	void shouldResolveFormIdForSiteAdmin(){
		Role role = Role.SITE_ADMIN;

		String formId = invitationFormIdResolver.getFormId(role);

		assertEquals("siteFormId", formId);
	}

	@Test
	void shouldResolveFormIdForSiteSupport(){
		Role role = Role.SITE_SUPPORT;

		String formId = invitationFormIdResolver.getFormId(role);

		assertEquals("siteFormId", formId);
	}

	@Test
	void shouldResolveFormIdForCommunityAdmin(){
		Role role = Role.COMMUNITY_ADMIN;

		String formId = invitationFormIdResolver.getFormId(role);

		assertEquals("communityFormId", formId);
	}

	@Test
	void shouldResolveFormIdForProjectAdmin(){
		Role role = Role.PROJECT_ADMIN;

		String formId = invitationFormIdResolver.getFormId(role);

		assertEquals("projectFormId", formId);
	}

	@Test
	void shouldResolveFormIdForProjectUser(){
		Role role = Role.PROJECT_USER;

		String formId = invitationFormIdResolver.getFormId(role);

		assertEquals("projectFormId", formId);
	}
}
