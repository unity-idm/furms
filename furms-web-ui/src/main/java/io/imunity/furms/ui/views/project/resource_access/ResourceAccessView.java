/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.resource_access;

import static com.vaadin.flow.component.icon.VaadinIcon.CHEVRON_DOWN_SMALL;
import static com.vaadin.flow.component.icon.VaadinIcon.CHEVRON_RIGHT_SMALL;
import static com.vaadin.flow.component.icon.VaadinIcon.MINUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.REFRESH;
import static com.vaadin.flow.component.icon.VaadinIcon.SEARCH;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.vaadin.gatanaso.MultiselectComboBox;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.StatusLayout;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

@Route(value = "project/admin/resource/access", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.resource-access.page.title")
public class ResourceAccessView extends FurmsViewComponent {

	public final String projectId;
	public final ResourceAccessViewService resourceAccessViewService;

	public final TreeGrid<ResourceAccessModel> treeGrid;
	public final MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>(getTranslation("view.project-admin.resource-access.multi-combo-box.filter"));
	public final TextField searchTextField = new TextField();

	ResourceAccessView(ProjectService projectService, ProjectAllocationService projectAllocationService, ResourceAccessService resourceAccessService) {
		this.projectId = getCurrentResourceId();
		this.resourceAccessViewService = new ResourceAccessViewService(projectService, projectAllocationService, resourceAccessService, projectId);
		this.treeGrid = new DenseTreeGrid<>();
		fillTreeGrid();

		getContent().add(createHeaderLayout(), createSearchFilterLayout(), treeGrid);
	}

	private HorizontalLayout createHeaderLayout() {
		return new ViewHeaderLayout(getTranslation("view.project-admin.resource-access.page.header"));
	}

	private HorizontalLayout createSearchFilterLayout() {
		searchTextField.setPlaceholder(getTranslation("view.project-admin.resource-access.field.search"));
		searchTextField.setPrefixComponent(SEARCH.create());
		searchTextField.setValueChangeMode(ValueChangeMode.EAGER);
		searchTextField.setClearButtonVisible(true);

		searchTextField.addValueChangeListener(event -> {
			String value = searchTextField.getValue().toLowerCase();
			reloadGrid(value, multiselectComboBox.getValue());
		});

		Set<String> allocations = resourceAccessViewService.getAllocations();

		multiselectComboBox.setItems(allocations);
		multiselectComboBox.addSelectionListener(event -> {
			reloadGrid(searchTextField.getValue().toLowerCase(), event.getAllSelectedItems());
		});

		HorizontalLayout search = new HorizontalLayout(searchTextField);
		search.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		search.setAlignItems(FlexComponent.Alignment.END);
		search.setWidthFull();
		HorizontalLayout filters = new HorizontalLayout(multiselectComboBox);
		filters.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

		return new HorizontalLayout(filters, search);
	}

	private void reloadGrid(String value, Set<String> allSelectedItems) {
		Map<ResourceAccessModel, List<ResourceAccessModel>> filteredUsers = resourceAccessViewService.loadDataWithFilters(value, allSelectedItems);
		treeGrid.setItems(filteredUsers.keySet(), x -> filteredUsers.getOrDefault(x, emptyList()));
	}

	private void reloadGrid() {
		Set<ResourceAccessModel> currentExpandedItems = resourceAccessViewService.loadDataWithFilters(searchTextField.getValue().toLowerCase(), multiselectComboBox.getValue())
			.keySet().stream()
			.filter(treeGrid::isExpanded)
			.collect(Collectors.toSet());
		resourceAccessViewService.reloadUserGrants();
		Map<ResourceAccessModel, List<ResourceAccessModel>> filteredUsers =
			resourceAccessViewService.loadDataWithFilters(searchTextField.getValue().toLowerCase(), multiselectComboBox.getValue());
		treeGrid.setItems(filteredUsers.keySet(), x -> filteredUsers.getOrDefault(x, emptyList()));
		treeGrid.expand(currentExpandedItems);
	}

	private void fillTreeGrid() {
		GridActionMenu contextMenu2 = new GridActionMenu();
		contextMenu2.addItem(new MenuButton(
				getTranslation("view.project-admin.resource-access.grid.context-menu.expand"), CHEVRON_DOWN_SMALL),
			event -> treeGrid.expand(resourceAccessViewService.getData().keySet())
		);
		contextMenu2.addItem(new MenuButton(
				getTranslation("view.project-admin.resource-access.grid.context-menu.collapse"), CHEVRON_RIGHT_SMALL),
			event -> treeGrid.collapse(resourceAccessViewService.getData().keySet())
		);
		treeGrid.addHierarchyColumn(ResourceAccessModel::getFullName)
			.setHeader(new HorizontalLayout(contextMenu2.getTarget(), 
					new Label(getTranslation("view.project-admin.resource-access.grid.column.1"))))
			.setSortable(true)
			.setFlexGrow(25);
		treeGrid.addColumn(ResourceAccessModel::getEmail)
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.3"))
			.setSortable(true)
			.setFlexGrow(25);
		treeGrid.addColumn(ResourceAccessModel::getAllocation)
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.4"))
			.setSortable(true)
			.setFlexGrow(25);
		treeGrid.addColumn(ResourceAccessModel::getAccess)
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.5"))
			.setSortable(true)
			.setFlexGrow(12);
		treeGrid.addComponentColumn(this::getStatusLayout)
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.6"))
			.setSortable(true)
			.setFlexGrow(13);
		treeGrid.addComponentColumn(resourceAccessModel -> {
				if(isRootNode(resourceAccessModel) || resourceAccessModel.isAccessible())
					return new Div();
				if(resourceAccessViewService.isGrantOrRevokeAvailable(resourceAccessModel))
					return getGridActionsButtonLayout(resourceAccessModel);
				return getGridRefreshButtonLayout();
			})
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.7"))
			.setWidth("6em");
		treeGrid.setItems(resourceAccessViewService.getData().keySet(), key -> resourceAccessViewService.getData().getOrDefault(key, emptyList()));
	}

	private boolean isRootNode(ResourceAccessModel resourceAccessModel) {
		return resourceAccessModel.getEmail() != null;
	}

	private Component getGridRefreshButtonLayout() {
		GridActionMenu contextMenu = new GridActionMenu();
		contextMenu.addItem(new MenuButton(getTranslation("view.project-admin.resource-access.grid.context-menu.refresh"),
			REFRESH), e -> reloadGrid()
		);
		return new GridActionsButtonLayout(contextMenu.getTarget());
	}

	private HorizontalLayout getStatusLayout(ResourceAccessModel resourceAccessModel) {
		return new StatusLayout(resourceAccessModel.getStatus(), resourceAccessModel.getMessage(), getContent());
	}


	private GridActionsButtonLayout getGridActionsButtonLayout(ResourceAccessModel resourceAccessModel) {
		GridActionMenu contextMenu = new GridActionMenu();
		if(resourceAccessViewService.isRevokeAvailable(resourceAccessModel)){
			contextMenu.addItem(
				new MenuButton(getTranslation("view.project-admin.resource-access.grid.context-menu.revoke"), MINUS_CIRCLE),
				event -> {
					resourceAccessViewService.revokeAccess(resourceAccessModel);
					reloadGrid();
				});
		} else {
			contextMenu.addItem(
				new MenuButton(getTranslation("view.project-admin.resource-access.grid.context-menu.grant"), PLUS_CIRCLE),
				event -> {
					resourceAccessViewService.grantAccess(resourceAccessModel);
					reloadGrid();
				});
		}
		return new GridActionsButtonLayout(contextMenu.getTarget());
	}

}
