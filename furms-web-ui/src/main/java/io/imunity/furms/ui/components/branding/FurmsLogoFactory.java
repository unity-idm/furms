/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.branding;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FurmsLogoFactory {

	private final SiteService siteService;
	private final CommunityService communityService;
	private final ProjectService projectService;

	public FurmsLogoFactory(SiteService siteService, CommunityService communityService, ProjectService projectService) {
		this.siteService = siteService;
		this.communityService = communityService;
		this.projectService = projectService;
	}

	public FurmsLogo create() {
		return new FurmsLogo(findLogo());
	}

	private Optional<FurmsImage> findLogo() {
		final FurmsViewUserContext context = FurmsViewUserContext.getCurrent();
		switch (context.viewMode) {
			case SITE:
				return siteService.findById(context.id)
					.map(Site::getLogo);
			case COMMUNITY:
				return communityService.findById(context.id)
					.map(Community::getLogo);
			case PROJECT:
				return projectService.findById(context.id)
					.map(Project::getLogo);
			default:
				return Optional.empty();
		}
	}
}
