/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

import java.util.Collections;
import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Route(value = "site/admin/resource/credits", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.resource-credits.page.title")
public class ResourceCreditsView extends FurmsViewComponent {

	private final ResourceCreditService resourceCreditService;
	private final Grid<ResourceCreditViewModel> grid;
	private final ResourceTypeComboBoxModelResolver resolver;

	public ResourceCreditsView(ResourceCreditService resourceCreditService, ResourceTypeService resourceTypeService) {
		this.resourceCreditService = resourceCreditService;
		this.grid = createCommunityGrid();
		this.resolver = new ResourceTypeComboBoxModelResolver(resourceTypeService.findAll(getCurrentResourceId()));

		Button addButton = createAddButton();
		loadGridContent();

		getContent().add(createHeaderLayout(addButton), new HorizontalLayout(grid));
	}

	private HorizontalLayout createHeaderLayout(Button addButton) {
		return new ViewHeaderLayout(getTranslation("view.site-admin.resource-credits.header"), addButton);
	}

	private Button createAddButton() {
		Button addButton = new Button(getTranslation("view.site-admin.resource-credits.button.add"), PLUS_CIRCLE.create());
		addButton.addClickListener(x -> UI.getCurrent().navigate(ResourceCreditFormView.class));
		return addButton;
	}

	private Grid<ResourceCreditViewModel> createCommunityGrid() {
		Grid<ResourceCreditViewModel> grid = new SparseGrid<>(ResourceCreditViewModel.class);

		grid.addComponentColumn(c -> new RouterLink(c.name, ResourceCreditFormView.class, c.id))
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.1"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(c -> resolver.getName(c.resourceTypeId))
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.2"))
			.setSortable(true);
		grid.addColumn(c -> c.amount.toPlainString() + " " + resolver.getResourceType(c.resourceTypeId).unit.name())
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.3"))
			.setSortable(true);
		grid.addColumn(c -> c.createTime.toLocalDate())
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.4"))
			.setSortable(true);
		grid.addColumn(c -> c.startTime.toLocalDate())
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.5"))
			.setSortable(true);
		grid.addColumn(c -> c.endTime.toLocalDate())
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.6"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.7"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(ResourceCreditViewModel resourceTypeViewModel) {
		return new GridActionsButtonLayout(
			createContextMenu(resourceTypeViewModel.id, resourceTypeViewModel.name)
		);
	}

	private Component createContextMenu(String resourceTypeId, String resourceCreditName) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.resource-credits.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(ResourceCreditFormView.class, resourceTypeId)
		);

		Dialog confirmDialog = createConfirmDialog(resourceTypeId, resourceCreditName);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.resource-credits.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(String resourceTypeId, String resourceCreditName) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.site-admin.resource-credits.dialog.text", resourceCreditName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			handleExceptions(() -> resourceCreditService.delete(resourceTypeId));
			loadGridContent();
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadServicesViewsModels());
	}

	private List<ResourceCreditViewModel> loadServicesViewsModels() {
		return handleExceptions(() -> resourceCreditService.findAll(getCurrentResourceId()))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(ResourceCreditViewModelMapper::map)
			.sorted(comparing(resourceTypeViewModel -> resourceTypeViewModel.name.toLowerCase()))
			.collect(toList());
	}
}
