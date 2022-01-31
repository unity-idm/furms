/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.logs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.audit_log.AuditLogService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.components.AuditLogDetailsComponentFactory;
import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsDateTimePicker;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.administrators.SearchLayout;
import io.imunity.furms.ui.user_context.UIContext;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOWN;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;
import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;
import static java.util.Comparator.comparing;

@Route(value = "fenix/admin/auditLog", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.audit-log.page.title")
public class AuditLogView extends FurmsViewComponent {
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final String fieldWidth = "calc(var(--vaadin-text-field-default-width) * 0.85)";
	private static final ObjectMapper mapper = new ObjectMapper();
	private final AuditLogService auditLogService;

	private final ZoneId browserZoneId;

	private final Grid<AuditLogGridModel> grid;
	private final FurmsDateTimePicker startDateTimePicker;
	private final FurmsDateTimePicker endDateTimePicker;
	private final MultiselectComboBox<FURMSUser> userComboBox;
	private final MultiselectComboBox<Operation> operationComboBox;
	private final MultiselectComboBox<Action> actionComboBox;
	private final SearchLayout searchLayout;

	private Set<AuditLogGridModel> data;

	AuditLogView(AuditLogService auditLogService, UserService userService) {
		this.auditLogService = auditLogService;

		browserZoneId = UIContext.getCurrent().getZone();

		grid = createCommunityGrid();

		userComboBox = new AuditLogMultiSelectComboBox<>(fieldWidth);
		userComboBox.setItems(userService.getAllUsers());
		userComboBox.setItemLabelGenerator(usr -> usr.email);
		userComboBox.addValueChangeListener(event -> reloadGrid());
		userComboBox.setPlaceholder(getTranslation("view.fenix-admin.audit-log.placeholder.user"));

		operationComboBox = new AuditLogMultiSelectComboBox<>(fieldWidth);
		operationComboBox.setItems(Operation.values());
		operationComboBox.setItemLabelGenerator(operation -> getTranslation("view.fenix-admin.audit-log.operation." + operation));
		operationComboBox.addValueChangeListener(event -> reloadGrid());
		operationComboBox.setPlaceholder(getTranslation("view.fenix-admin.audit-log.placeholder.type"));

		actionComboBox = new AuditLogMultiSelectComboBox<>(fieldWidth);
		actionComboBox.setItems(Action.values());
		actionComboBox.setItemLabelGenerator(action -> getTranslation("view.fenix-admin.audit-log.action." + action));
		actionComboBox.addValueChangeListener(event -> reloadGrid());
		actionComboBox.setPlaceholder(getTranslation("view.fenix-admin.audit-log.placeholder.action"));

		searchLayout = new SearchLayout();
		searchLayout.addValueChangeGridReloader(this::reloadGridForSearch);
		searchLayout.setWidth(fieldWidth);
		searchLayout.getStyle().set("margin-top", "0.7em");
		searchLayout.getStyle().set("margin-right", "0.7em");

		ZonedDateTime now = ZonedDateTime.now(browserZoneId);
		startDateTimePicker = new FurmsDateTimePicker(() -> now.minusHours(24).toLocalTime());
		startDateTimePicker.setValue(now.minusHours(24));
		startDateTimePicker.addValueChangeListener(event -> reloadGrid());
		startDateTimePicker.setWidth(fieldWidth);

		endDateTimePicker = new FurmsDateTimePicker(now::toLocalTime);
		endDateTimePicker.setValue(now.plusMinutes(1));
		endDateTimePicker.addValueChangeListener(event -> reloadGrid());
		endDateTimePicker.setWidth(fieldWidth);

		FlexLayout searchLayout = new FlexLayout(userComboBox, operationComboBox, actionComboBox, this.searchLayout);
		searchLayout.setAlignItems(FlexComponent.Alignment.START);
		searchLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
		searchLayout.setFlexDirection(FlexLayout.FlexDirection.ROW);

		getContent().add(
			new VerticalLayout(
				new HorizontalLayout(startDateTimePicker, endDateTimePicker),
				searchLayout,
				grid
			)
		);
		reloadGrid();
	}

	private void reloadGrid() {
		data = auditLogService.findBy(
			startDateTimePicker.getValue(),
			endDateTimePicker.getValue(),
			userComboBox.getSelectedItems(),
			actionComboBox.getSelectedItems().stream()
				.map(Action::getPersistentId)
				.collect(Collectors.toSet()),
			operationComboBox.getSelectedItems().stream()
				.map(Operation::getPersistentId)
				.collect(Collectors.toSet())
		).stream()
			.map(auditLog -> AuditLogGridModel.builder()
				.id(auditLog.resourceId)
				.timestamp(convertToZoneTime(auditLog.utcTimestamp, browserZoneId))
				.originator(auditLog.originator.email)
				.action(auditLog.action)
				.operation(auditLog.operationCategory)
				.name(auditLog.operationSubject)
				.data(getData(auditLog))
				.build()
			)
			.collect(Collectors.toSet());
		reloadGridForSearch();
	}

	private void reloadGridForSearch() {
		grid.setItems(
			data.stream()
				.filter(model -> rowContains(model, searchLayout.getSearchText(), searchLayout))

		);
	}

	private boolean rowContains(AuditLogGridModel row, String value, SearchLayout searchLayout) {
		String lowerCaseValue = value.toLowerCase();
		return searchLayout.getSearchText().isEmpty()
			|| row.originator.toLowerCase().contains(lowerCaseValue)
			|| row.id.toLowerCase().contains(lowerCaseValue)
			|| row.operation.name().toLowerCase().contains(lowerCaseValue)
			|| row.action.name().toLowerCase().contains(lowerCaseValue);
	}

	private Map<String, Object> getData(AuditLog auditLog) {
		if(auditLog.dataJson == null)
			return new HashMap<>();
		try {
			return mapper.convertValue(mapper.readTree(auditLog.dataJson), new TypeReference<>() {});
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private Grid<AuditLogGridModel> createCommunityGrid() {
		Grid<AuditLogGridModel> grid = new DenseGrid<>(AuditLogGridModel.class);

		Column<AuditLogGridModel> timestamp = grid.addComponentColumn(model -> {
				if(model.data.isEmpty())
					return new Div(new Label(model.timestamp.format(dateTimeFormatter)));
				Icon icon = grid.isDetailsVisible(model) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create();
				return new Div(icon, new Label(model.timestamp.format(dateTimeFormatter)));
			})
			.setHeader(getTranslation("view.fenix-admin.audit-log.grid.1"))
			.setSortable(true)
			.setComparator(model -> model.timestamp);
		grid.addColumn(model -> model.originator)
			.setHeader(getTranslation("view.fenix-admin.audit-log.grid.2"))
			.setSortable(true)
			.setComparator(model -> model.originator);
		grid.addColumn(model -> getTranslation("view.fenix-admin.audit-log.operation." + model.operation))
			.setHeader(getTranslation("view.fenix-admin.audit-log.grid.3"))
			.setSortable(true)
			.setComparator(comparing(model -> model.operation));
		grid.addColumn(model -> getTranslation("view.fenix-admin.audit-log.action." + model.action))
			.setHeader(getTranslation("view.fenix-admin.audit-log.grid.4"))
			.setSortable(true)
			.setComparator(comparing(model -> model.action));
		grid.addColumn(model -> model.name)
			.setHeader(getTranslation("view.fenix-admin.audit-log.grid.5"))
			.setSortable(true)
			.setComparator(comparing(model -> model.name));
		grid.addColumn(model -> model.id)
			.setHeader(getTranslation("view.fenix-admin.audit-log.grid.6"))
			.setSortable(true)
			.setComparator(comparing(model -> model.id));

		grid.sort(ImmutableList.of(new GridSortOrder<>(timestamp, SortDirection.DESCENDING)));
		grid.setItemDetailsRenderer(new ComponentRenderer<>(c -> AuditLogDetailsComponentFactory
			.create(c.data)));
		grid.setSelectionMode(Grid.SelectionMode.NONE);

		return grid;
	}

}
