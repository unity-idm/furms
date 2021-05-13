/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.resource_access;

import com.vaadin.componentfactory.Tooltip;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static java.util.Collections.emptyList;

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
		this.treeGrid = new TreeGrid<>();
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
		resourceAccessViewService.reloadUserGrants();
		Map<ResourceAccessModel, List<ResourceAccessModel>> filteredUsers =
			resourceAccessViewService.loadDataWithFilters(searchTextField.getValue().toLowerCase(), multiselectComboBox.getValue());
		treeGrid.setItems(filteredUsers.keySet(), x -> filteredUsers.getOrDefault(x, emptyList()));
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
		treeGrid.addHierarchyColumn(resourceAccessModel -> Optional.ofNullable(resourceAccessModel.getFirstName()).orElse(""))
			.setHeader(new HorizontalLayout(contextMenu2.getTarget(), new Label(getTranslation("view.project-admin.resource-access.grid.column.1"))))
			.setSortable(true);
		treeGrid.addColumn(ResourceAccessModel::getLastName)
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.2"))
			.setSortable(true);
		treeGrid.addColumn(ResourceAccessModel::getEmail)
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.3"))
			.setSortable(true);
		treeGrid.addColumn(ResourceAccessModel::getAllocation)
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.4"))
			.setSortable(true);
		treeGrid.addColumn(ResourceAccessModel::getAccess)
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.5"))
			.setSortable(true);
		treeGrid.addComponentColumn(this::getStatusLayout)
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.6"))
			.setSortable(true);
		treeGrid.addComponentColumn(resourceAccessModel -> {
			if(resourceAccessViewService.isGrantOrRevokeAvailable(resourceAccessModel)) {
				return getGridActionsButtonLayout(resourceAccessModel);
			}
			return new Div();
		})
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.7"));
		treeGrid.setItems(resourceAccessViewService.getData().keySet(), key -> resourceAccessViewService.getData().getOrDefault(key, emptyList()));
	}

	private HorizontalLayout getStatusLayout(ResourceAccessModel resourceAccessModel) {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		if(resourceAccessModel.getStatus() != null)
			horizontalLayout.add(new Text(resourceAccessModel.getStatus()));
		if(resourceAccessModel.getMessage() != null){
			Tooltip tooltip = new Tooltip();
			Icon icon = WARNING.create();
			tooltip.attachToComponent(icon);
			tooltip.add(resourceAccessModel.getMessage());
			getContent().add(tooltip);
			horizontalLayout.add(icon);
		}
		return horizontalLayout;
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
