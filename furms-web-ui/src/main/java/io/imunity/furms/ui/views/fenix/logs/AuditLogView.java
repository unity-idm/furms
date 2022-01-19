/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.logs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOWN;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;
import static io.imunity.furms.ui.views.TimeConstants.DEFAULT_END_TIME;
import static io.imunity.furms.ui.views.TimeConstants.DEFAULT_START_TIME;
import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;
import static java.util.Comparator.comparing;

@Route(value = "fenix/admin/auditLog", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.audit-log.page.title")
public class AuditLogView extends FurmsViewComponent {
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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


	AuditLogView(AuditLogService auditLogService, UserService userService) {
		this.auditLogService = auditLogService;

		browserZoneId = UIContext.getCurrent().getZone();

		grid = createCommunityGrid();

		startDateTimePicker = new FurmsDateTimePicker(() -> DEFAULT_START_TIME);
		startDateTimePicker.addValueChangeListener(event -> reloadGrid());

		endDateTimePicker = new FurmsDateTimePicker(() -> DEFAULT_END_TIME);
		endDateTimePicker.addValueChangeListener(event -> reloadGrid());

		userComboBox = new MultiselectComboBox<>();
		userComboBox.setClassName("abc");
		userComboBox.setItems(userService.getAllUsers());
		userComboBox.setItemLabelGenerator(usr -> usr.email);
		userComboBox.setRequired(true);
		userComboBox.addValueChangeListener(event -> reloadGrid());
		userComboBox.setPlaceholder(getTranslation("view.fenix-admin.audit-log.placeholder.user"));

		operationComboBox = new MultiselectComboBox<>();
		operationComboBox.setItems(Operation.values());
		operationComboBox.setItemLabelGenerator(operation -> getTranslation("view.fenix-admin.audit-log.operation." + operation));
		operationComboBox.setRequired(true);
		operationComboBox.addValueChangeListener(event -> reloadGrid());
		operationComboBox.setPlaceholder(getTranslation("view.fenix-admin.audit-log.placeholder.type"));

		actionComboBox = new MultiselectComboBox<>();
		actionComboBox.setItems(Action.values());
		actionComboBox.setItemLabelGenerator(action -> getTranslation("view.fenix-admin.audit-log.action." + action));
		actionComboBox.setRequired(true);
		actionComboBox.addValueChangeListener(event -> reloadGrid());
		actionComboBox.setPlaceholder(getTranslation("view.fenix-admin.audit-log.placeholder.action"));

		searchLayout = new SearchLayout();
		searchLayout.addValueChangeGridReloader(this::reloadGrid);

		getContent().add(
			new VerticalLayout(
				new HorizontalLayout(startDateTimePicker, endDateTimePicker),
				new HorizontalLayout(userComboBox, operationComboBox, actionComboBox, searchLayout),
				grid
			)
		);
	}

	private void reloadGrid() {
		if(userComboBox.getSelectedItems().isEmpty() || actionComboBox.getSelectedItems().isEmpty() || operationComboBox.getSelectedItems().isEmpty())
			return;
		Set<AuditLog> auditLogs = auditLogService.findBy(
			startDateTimePicker.getValue(),
			endDateTimePicker.getValue(),
			userComboBox.getSelectedItems(),
			actionComboBox.getSelectedItems().stream()
				.map(Action::getPersistentId)
				.collect(Collectors.toSet()),
			operationComboBox.getSelectedItems().stream()
				.map(Operation::getPersistentId)
				.collect(Collectors.toSet()),
			searchLayout.getSearchText()
		);
		grid.setItems(
			auditLogs.stream()
				.map(auditLog -> AuditLogGridModel.builder()
					.timestamp(convertToZoneTime(auditLog.utcTimestamp, browserZoneId))
					.originator(auditLog.originator.email)
					.action(auditLog.action)
					.operation(auditLog.operationCategory)
					.name(auditLog.operationSubject)
					.data(getData(auditLog))
					.build()
				)
		);
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

		grid.addComponentColumn(model -> {
				Icon icon = grid.isDetailsVisible(model) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create();
				return new Div(icon, new Label(model.timestamp.format(dateTimeFormatter)));
			})
			.setHeader(getTranslation("view.fenix-admin.audit-log.grid.1"))
			.setSortable(true);
		grid.addColumn(model -> model.originator)
			.setHeader(getTranslation("view.fenix-admin.audit-log.grid.2"))
			.setSortable(true)
			.setComparator(model -> getTranslation("view.fenix-admin.audit-log.operation." + model.originator));
		grid.addColumn(model -> model.operation)
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

		grid.setItemDetailsRenderer(new ComponentRenderer<>(c -> AuditLogDetailsComponentFactory
			.create(c.data)));
		grid.setSelectionMode(Grid.SelectionMode.NONE);

		return grid;
	}

}
