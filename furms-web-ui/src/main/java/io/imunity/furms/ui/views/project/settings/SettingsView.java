/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.settings;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.project.ProjectFormComponent;
import io.imunity.furms.ui.project.ProjectViewModel;
import io.imunity.furms.ui.views.community.projects.ProjectsView;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

@Route(value = "project/admin/settings", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.settings.page.title")
public class SettingsView extends FurmsViewComponent {
	private final Binder<ProjectViewModel> binder = new BeanValidationBinder<>(ProjectViewModel.class);
	private final ProjectFormComponent projectFormComponent;
	private final ProjectService projectService;

	SettingsView(ProjectService projectService) {
		this.projectService = projectService;
		this.projectFormComponent = new ProjectFormComponent(binder, false);


//		binder.addStatusChangeListener(status -> saveButton.setEnabled(binder.isValid()));

	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.community-admin.project.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(ProjectsView.class));
		return closeButton;
	}
}
