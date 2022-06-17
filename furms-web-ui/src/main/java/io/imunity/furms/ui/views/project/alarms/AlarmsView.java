/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.alarms;

import com.vaadin.componentfactory.Tooltip;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.alarms.AlarmService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.utils.CommonExceptionsHandler;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Route(value = "project/admin/alarms", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.alarms.page.title")
public class AlarmsView extends FurmsViewComponent {
	private final AlarmService alarmService;
	private final ProjectAllocationService projectAllocationService;
	private final Grid<AlarmGridModel> grid;
	private final ProjectId projectId;

	AlarmsView(AlarmService alarmService, ProjectAllocationService projectAllocationService) {
		this.alarmService = alarmService;
		this.projectAllocationService = projectAllocationService;
		this.grid = createAlarmGrid();
		this.projectId = new ProjectId(getCurrentResourceId());

		loadGridContent();
		ViewHeaderLayout viewHeaderLayout = new ViewHeaderLayout(getTranslation("view.project-admin.alarms.page.header"), createAddButton());
		getContent().add(viewHeaderLayout, grid);
	}

	private Button createAddButton() {
		Button addButton = new Button(getTranslation("view.project-admin.alarms.page.button.add"), PLUS_CIRCLE.create());
		addButton.addClickListener(x -> UI.getCurrent().navigate(AlarmFormView.class));
		return addButton;
	}

	private void loadGridContent() {
		grid.setItems(loadPolicyDocumentsGridModels());
	}

	private List<AlarmGridModel> loadPolicyDocumentsGridModels() {
		Map<ProjectAllocationId, String> collect = projectAllocationService.findAllWithRelatedObjects(projectId).stream()
			.collect(Collectors.toMap(x -> x.id, x -> x.name));
		return handleExceptions(() -> alarmService.findAll(projectId))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(alarm -> AlarmGridModel.builder()
				.alarmId(alarm.id)
				.name(alarm.name)
				.allocationName(collect.get(alarm.projectAllocationId))
				.threshold(alarm.threshold)
				.allUsers(alarm.allUsers)
				.fired(alarm.fired)
				.users(alarm.alarmUserEmails)
				.build()
			)
			.sorted(comparing(projectViewModel -> projectViewModel.name.toLowerCase()))
			.collect(toList());
	}

	private Grid<AlarmGridModel> createAlarmGrid() {
		Grid<AlarmGridModel> grid = new DenseGrid<>(AlarmGridModel.class);

		grid.addComponentColumn(model -> new RouterLink(model.name, AlarmFormView.class, model.id.id.toString()))
			.setHeader(getTranslation("view.project-admin.alarms.page.grid.1"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(model -> model.allocationName)
			.setHeader(getTranslation("view.project-admin.alarms.page.grid.2"))
			.setSortable(true);
		grid.addColumn(model -> model.threshold + "%")
			.setHeader(getTranslation("view.project-admin.alarms.page.grid.3"))
			.setSortable(true);
		grid.addComponentColumn(model -> new DisableCheckbox(model.allUsers))
			.setHeader(getTranslation("view.project-admin.alarms.page.grid.4"))
			.setSortable(true)
			.setComparator(model -> model.allUsers);
		grid.addComponentColumn(model -> {
			Label label = new Label(
				model.users.stream()
					.limit(3)
					.collect(Collectors.joining()) + (model.users.size() > 3 ? "..." : "")
			);

			Tooltip tooltip = new Tooltip();
			tooltip.add(String.join(", ", model.users));
			tooltip.attachToComponent(label);
			getContent().add(tooltip);
			return label;
		})
			.setHeader(getTranslation("view.project-admin.alarms.page.grid.5"))
			.setSortable(true)
			.setComparator(model -> model.users.toString());
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.project-admin.alarms.page.grid.6"))
			.setTextAlign(ColumnTextAlign.END);
		grid.setClassNameGenerator(model -> model.fired ? "light-red-row" : "usual-row");

		return grid;
	}

	private HorizontalLayout createLastColumnContent(AlarmGridModel model) {
		Component contextMenu = createContextMenu(projectId, model.id, model.name);
		return new GridActionsButtonLayout(contextMenu);
	}


	private Component createContextMenu(ProjectId projectId, AlarmId alarmId, String alarmName) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.project-admin.alarms.page.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(AlarmFormView.class, alarmId.id.toString())
		);

		Dialog confirmDialog = createConfirmDialog(projectId, alarmId, alarmName);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.project-admin.alarms.page.menu.remove"), TRASH),
			event -> confirmDialog.open()
		);

		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(ProjectId projectId, AlarmId alarmId, String alarmName) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.project-admin.alarms.page.dialog.text", alarmName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			try {
				alarmService.remove(projectId, alarmId);
				loadGridContent();
			} catch (RuntimeException e) {
				CommonExceptionsHandler.showExceptionBasedNotificationError(e, "Could not remove alarm.");
			}
		});
		return furmsDialog;
	}

	private static class DisableCheckbox extends Checkbox {
		DisableCheckbox(boolean value) {
			super(value);
			this.getStyle().set("pointer-events", "none");
			this.setReadOnly(true);
		}
	}
}
