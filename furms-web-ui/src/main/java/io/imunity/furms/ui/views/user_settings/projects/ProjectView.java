/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.projects;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.user_settings.UserSettingsMenu;

import java.util.Optional;

import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.function.Function.identity;

@Route(value = "users/settings/project", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.projects.page.title")
class ProjectView extends FurmsViewComponent {
	private final ProjectService projectService;
	private BreadCrumbParameter breadCrumbParameter;

	public ProjectView(ProjectService projectService) {
		this.projectService = projectService;
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String projectId) {
		Project project = handleExceptions(() -> projectService.findById(projectId))
			.flatMap(identity())
			.orElseThrow(IllegalStateException::new);
		breadCrumbParameter = new BreadCrumbParameter(project.getId(), project.getName(), "ALLOCATION");
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
