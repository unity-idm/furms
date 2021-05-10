/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.resource_access;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.project.ProjectViewModel;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.icon.VaadinIcon.SEARCH;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = "project/admin/resource/access", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.resource-access.page.title")
public class ResourceAccessView extends FurmsViewComponent {

	ProjectService projectService;
	ProjectAllocationService projectAllocationService;

	ResourceAccessView() {
		List<FURMSUser> users = projectService.findAllUsers("c", getCurrentResourceId());
		Set<ProjectAllocationResolved> allocations = projectAllocationService.findAllWithRelatedObjects("c", getCurrentResourceId());

		TreeGrid<ResourceAccessModel> treeGrid = new TreeGrid<>();

		fill(users, allocations, treeGrid);


		treeGrid.collapse();
		treeGrid.expand();
	}

	private HorizontalLayout createHeaderLayout() {
		return new ViewHeaderLayout(getTranslation("view.community-admin.projects.header"));
	}

	private HorizontalLayout createSearchFilterLayout(Grid<ProjectViewModel> grid, Button addButton) {
		TextField textField = new TextField();
		textField.setPlaceholder(getTranslation("view.community-admin.projects.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.setClearButtonVisible(true);
		textField.addValueChangeListener(event -> {
			String value = textField.getValue().toLowerCase();
//			List<ProjectViewModel> filteredUsers = loadProjectsViewsModels().stream()
//				.filter(project -> project.matches(value))
//				.collect(toList());
//			grid.setItems(filteredUsers);
			//TODO This is a work around to fix disappearing text cursor
			addButton.focus();
			textField.focus();
		});

		HorizontalLayout search = new HorizontalLayout(textField);
		search.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		return search;
	}

	private void fill(List<FURMSUser> users, Set<ProjectAllocationResolved> allocations, TreeGrid<ResourceAccessModel> treeGrid) {
		Map<ResourceAccessModel, List<ResourceAccessModel>> data = users.stream()
			.collect(Collectors.toMap(u ->
				ResourceAccessModel.builder()
					.firstName(u.firstName.orElse(""))
					.lastName(u.lastName.orElse(""))
					.email(u.email)
					.build(),
				u -> allocations.stream()
					.map(a -> ResourceAccessModel.builder()
						.allocation(a.name)
						.access("Enabled")
						.status("Implied")
						.accessible(a.resourceType.accessible)
						.build())
					.collect(Collectors.toList())
			));

		treeGrid.setItems(data.keySet(), data::get);
	}
}
