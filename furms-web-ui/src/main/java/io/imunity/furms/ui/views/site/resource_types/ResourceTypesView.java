/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_types;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.api.services.InfraServiceService;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

import java.util.Collections;
import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Route(value = "site/admin/resource/types", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.resource-types.page.title")
public class ResourceTypesView extends FurmsViewComponent {

	private final ResourceTypeService resourceTypeService;
	private final Grid<ResourceTypeViewModel> grid;
	private final ServiceComboBoxModelResolver resolver;

	public ResourceTypesView(ResourceTypeService resourceTypeService, InfraServiceService serviceService) {
		this.resourceTypeService = resourceTypeService;
		this.grid = createCommunityGrid();
		this.resolver = new ServiceComboBoxModelResolver(serviceService.findAll(getCurrentResourceId()));

		Button addButton = createAddButton();
		loadGridContent();

		getContent().add(createHeaderLayout(addButton), new HorizontalLayout(grid));
	}

	private HorizontalLayout createHeaderLayout(Button addButton) {
		return new ViewHeaderLayout(getTranslation("view.site-admin.resource-types.header"), addButton);
	}

	private Button createAddButton() {
		Button addButton = new Button(getTranslation("view.site-admin.resource-types.button.add"), PLUS_CIRCLE.create());
		addButton.addClickListener(x -> UI.getCurrent().navigate(ResourceTypeFormView.class));
		return addButton;
	}

	private Grid<ResourceTypeViewModel> createCommunityGrid() {
		Grid<ResourceTypeViewModel> grid = new SparseGrid<>(ResourceTypeViewModel.class);

		grid.addComponentColumn(c -> new RouterLink(c.name, ResourceTypeFormView.class, c.id))
			.setHeader(getTranslation("view.site-admin.resource-types.grid.column.1"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(c -> resolver.getName(c.serviceId))
			.setHeader(getTranslation("view.site-admin.resource-types.grid.column.2"))
			.setSortable(true);
		grid.addColumn(c -> c.type)
			.setHeader(getTranslation("view.site-admin.resource-types.grid.column.3"))
			.setSortable(true);
		grid.addColumn(c -> c.unit)
			.setHeader(getTranslation("view.site-admin.resource-types.grid.column.4"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.site-admin.resource-types.grid.column.5"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(ResourceTypeViewModel serviceViewModel) {
		return new GridActionsButtonLayout(
			createContextMenu(serviceViewModel.id, serviceViewModel.name)
		);
	}

	private Component createContextMenu(String serviceId, String resourceTypeName) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.resource-types.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(ResourceTypeFormView.class, serviceId)
		);

		Dialog confirmDialog = createConfirmDialog(serviceId, resourceTypeName);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.resource-types.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(String serviceId, String resourceTypeName) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.site-admin.resource-types.dialog.text", resourceTypeName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			getResultOrException(() -> resourceTypeService.delete(serviceId))
				.getThrowable()
				.ifPresent(throwable -> showErrorNotification(getTranslation(throwable.getMessage(), resourceTypeName)));
			loadGridContent();
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadServicesViewsModels());
	}

	private List<ResourceTypeViewModel> loadServicesViewsModels() {
		return handleExceptions(() -> resourceTypeService.findAll(getCurrentResourceId()))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(ResourceTypeViewModelMapper::map)
			.sorted(comparing(serviceViewModel -> serviceViewModel.name.toLowerCase()))
			.collect(toList());
	}
}
