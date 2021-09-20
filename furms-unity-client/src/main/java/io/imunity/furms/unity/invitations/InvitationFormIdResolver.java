/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.invitations;

import io.imunity.furms.domain.authz.roles.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class InvitationFormIdResolver {
	private final String fenixFormId;
	private final String siteFormId;
	private final String communityFormId;
	private final String projectFormId;

	InvitationFormIdResolver(
	              @Value("${furms.invitations.fenix-form}") String fenixFormId,
	              @Value("${furms.invitations.site-form}") String siteFormId,
	              @Value("${furms.invitations.community-form}") String communityFormId,
	              @Value("${furms.invitations.project-form}") String projectFormId) {
		this.fenixFormId = fenixFormId;
		this.siteFormId = siteFormId;
		this.communityFormId = communityFormId;
		this.projectFormId = projectFormId;
	}

	String getFormId(Role role) {
		switch (role){
			case FENIX_ADMIN :
				return fenixFormId;
			case SITE_ADMIN:
			case SITE_SUPPORT:
				return siteFormId;
			case COMMUNITY_ADMIN:
				return communityFormId;
			case PROJECT_ADMIN:
			case PROJECT_USER:
				return projectFormId;
			default:
				throw new IllegalArgumentException("This shouldn't happen, invitation always need role");
		}
	}
}
