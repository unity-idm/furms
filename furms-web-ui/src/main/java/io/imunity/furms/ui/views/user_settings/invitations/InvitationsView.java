/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.invitations;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.invitations.InvitationService;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.user_context.InvocationContext;
import io.imunity.furms.ui.views.user_settings.UserSettingsMenu;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.icon.VaadinIcon.CHECK_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.CLOSE_CIRCLE;
import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

@Route(value = "users/settings/invitations", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.invitations.page.title")
public class InvitationsView extends FurmsViewComponent {
	private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final Grid<InvitationGridModel> grid;
	private final ZoneId browserZoneId;
	private final InvitationService invitationService;

	InvitationsView(InvitationService invitationService) {
		this.invitationService = invitationService;
		this.browserZoneId = InvocationContext.getCurrent().getZone();

		Map<InvitationId, Checkbox> checkboxes = new HashMap<>();
		Component mainContextMenu = createMainContextMenu(invitationService, checkboxes);
		grid = createInvitationGrid(checkboxes, mainContextMenu);

		ViewHeaderLayout headerLayout = new ViewHeaderLayout(getTranslation("view.user-settings.invitations.page.header"));

		getContent().add(headerLayout, grid);

		loadGrid();
	}

	private Component createMainContextMenu(InvitationService invitationService, Map<InvitationId, Checkbox> checkboxes) {
		GridActionMenu contextMenu = new GridActionMenu();
		contextMenu.addItem(new MenuButton(
				getTranslation("view.user-settings.invitations.main.context-menu.confirm"), CHECK_CIRCLE),
			event -> {
				checkboxes.entrySet().stream()
					.filter(x -> x.getValue().getValue())
					.forEach(x -> invitationService.acceptBy(x.getKey()));
				loadGrid();
			}
		);
		contextMenu.addItem(new MenuButton(
				getTranslation("view.user-settings.invitations.main.context-menu.reject"), CLOSE_CIRCLE),
			event -> {
				checkboxes.entrySet().stream()
					.filter(x -> x.getValue().getValue())
					.forEach(x -> invitationService.deleteBy(x.getKey()));
				loadGrid();
			}
		);
		return contextMenu.getTarget();
	}

	private Grid<InvitationGridModel> createInvitationGrid(Map<InvitationId, Checkbox> checkboxes, Component mainContextMenu) {
		final Grid<InvitationGridModel> grid;
		grid = new SparseGrid<>(InvitationGridModel.class);

		grid.addComponentColumn(invitationGridModel -> {
			Checkbox checkbox = new Checkbox();
			checkboxes.put(invitationGridModel.id ,checkbox);
			return new HorizontalLayout(checkbox, new Label(invitationGridModel.invitationText));
		}).setHeader(new HorizontalLayout(mainContextMenu, new Label(getTranslation("view.user-settings.invitations.grid.1"))));
		grid.addColumn(x -> x.originator)
			.setHeader(getTranslation("view.user-settings.invitations.grid.2"))
			.setSortable(true);
		grid.addColumn(x -> x.expiration.format(dateTimeFormatter))
			.setHeader(getTranslation("view.user-settings.invitations.grid.3"))
			.setSortable(true);
		grid.addComponentColumn(x -> createContextMenu(x.id))
			.setHeader(getTranslation("view.user-settings.invitations.grid.4"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private void loadGrid() {
		Set<InvitationGridModel> collect = invitationService.findAllByCurrentUser()
			.stream()
			.map(invitation -> new InvitationGridModel(
				invitation.id,
				Optional.ofNullable(invitation.resourceName).map(y -> "'" + y + "'").orElse("") +
					" " +
					getTranslation("view.user-settings.invitations.grid.invitation.resource.type." + invitation.resourceId.type) +
					" " +
					getTranslation("view.user-settings.invitations.grid.invitation.role." + invitation.role.unityRoleValue),
				invitation.originator,
				convertToZoneTime(invitation.utcExpiredAt, browserZoneId).toLocalDateTime()
			))
			.collect(Collectors.toSet());
		grid.setItems(collect);
	}

	private Component createContextMenu(InvitationId id) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.user-settings.invitations.grid.context-menu.confirm"), CHECK_CIRCLE),
			event -> {
				invitationService.acceptBy(id);
				loadGrid();
			}
		);

		Dialog confirmDialog = createConfirmDialog(id);
		contextMenu.addItem(new MenuButton(
				getTranslation("view.user-settings.invitations.grid.context-menu.reject"), CLOSE_CIRCLE),
			event -> confirmDialog.open()
		);

		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(InvitationId invitationId) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.user-settings.invitations.page.removal.confirm"));
		furmsDialog.addConfirmButtonClickListener(event -> {
			invitationService.deleteBy(invitationId);
			loadGrid();
		});
		return furmsDialog;
	}
}
