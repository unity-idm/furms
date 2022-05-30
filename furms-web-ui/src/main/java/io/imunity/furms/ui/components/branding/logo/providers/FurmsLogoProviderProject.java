/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.branding.logo.providers;

import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.ViewMode;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class FurmsLogoProviderProject implements FurmsLogoProvider{
	private final ProjectService projectService;

	FurmsLogoProviderProject(ProjectService projectService) {
		this.projectService = projectService;
	}

	@Override
	public ViewMode getViewMode() {
		return ViewMode.PROJECT;
	}

	@Override
	public Optional<FurmsImage> getLogoForCurrentViewMode() {
		final FurmsViewUserContext context = FurmsViewUserContext.getCurrent();
		return projectService.findById(new ProjectId(context.id))
				.map(Project::getLogo);
	}
}
