/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;

import static com.vaadin.flow.component.grid.ColumnTextAlign.END;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.domain.ssh_key.SSHKey;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.user_settings.UserSettingsMenu;

@Route(value = "users/settings/ssh/keys", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.ssh-keys.page.title")
public class SSHKeysView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SSHKeyService sshKeysService;
	private final Grid<SSHKeyViewModel> grid;
	private final SiteComboBoxModelResolver resolver;
	private ZoneId zoneId;

	public SSHKeysView(SSHKeyService sshKeysService, AuthzService authzService, SiteService siteService) {
		this.sshKeysService = sshKeysService;
		this.grid = createSSHKeysGrid();
		this.resolver = new SiteComboBoxModelResolver(siteService.findAll());

		UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
			zoneId = ZoneId.of(extendedClientDetails.getTimeZoneId());
		});

		Button addButton = createAddButton();
		loadGridContent();
		getContent().add(createHeaderLayout(addButton), new HorizontalLayout(grid));
	}

	private HorizontalLayout createHeaderLayout(Button addButton) {
		return new ViewHeaderLayout(getTranslation("view.user-settings.ssh-keys.header"), addButton);
	}

	private Button createAddButton() {
		Button addButton = new Button(getTranslation("view.user-settings.ssh-keys.button.add"),
				PLUS_CIRCLE.create());
		addButton.addClickListener(x -> UI.getCurrent().navigate(SSHKeyFormView.class));
		return addButton;
	}

	private Grid<SSHKeyViewModel> createSSHKeysGrid() {
		Grid<SSHKeyViewModel> grid = new SparseGrid<>(SSHKeyViewModel.class);

		grid.addComponentColumn(k -> new RouterLink(k.getName(), SSHKeyFormView.class, k.id))
				.setHeader(getTranslation("view.user-settings.ssh-keys.grid.column.1"))
				.setSortable(true).setComparator(x -> x.getName().toLowerCase()).setResizable(true).setFlexGrow(1);
		grid.addColumn(k -> k.rowSiteId != null ? resolver.getName(k.rowSiteId) : "")
				.setHeader(getTranslation("view.user-settings.ssh-keys.grid.column.2"))
				.setSortable(true).setFlexGrow(1).setResizable(true);
		grid.addColumn(k -> SSHKey.getKeyFingerprint(k.getValue()))
				.setHeader(getTranslation("view.user-settings.ssh-keys.grid.column.3"))
				.setSortable(true).setResizable(true).setFlexGrow(10);

		grid.addColumn(k -> k.createTime.toLocalDate())
				.setHeader(getTranslation("view.user-settings.ssh-keys.grid.column.4"))
				.setSortable(true).setFlexGrow(1);

		grid.addComponentColumn(k -> createLastColumnContent(k, grid))
				.setHeader(getTranslation("view.user-settings.ssh-keys.grid.column.actions"))
				.setKey("actions").setTextAlign(END);

		return grid;
	}

	private Component createLastColumnContent(SSHKeyViewModel key, Grid<SSHKeyViewModel> grid) {
		return new GridActionsButtonLayout(createContextMenu(key, grid));
	}

	private Component createContextMenu(SSHKeyViewModel key, Grid<SSHKeyViewModel> grid) {
		GridActionMenu contextMenu = new GridActionMenu();
		contextMenu.setId(key.id);
		contextMenu.addItem(new MenuButton(getTranslation("view.sites.main.grid.item.menu.edit"), EDIT),
				event -> UI.getCurrent().navigate(SSHKeyFormView.class, key.id));
		contextMenu.addItem(
				new MenuButton(getTranslation("view.user-settings.ssh-keys.grid.menu.delete"), TRASH),
				e -> actionDeleteSSHKey(key, grid));

		getContent().add(contextMenu);

		return contextMenu.getTarget();
	}

	private void actionDeleteSSHKey(SSHKeyViewModel key, Grid<SSHKeyViewModel> grid) {
		FurmsDialog cancelDialog = new FurmsDialog(getTranslation(
				"view.user-settings.ssh-keys.main.confirmation.dialog.delete", key.getName()));
		cancelDialog.addConfirmButtonClickListener(event -> {
			try {
				sshKeysService.delete(key.id);
				showSuccessNotification(getTranslation(
						"view.user-settings.ssh-keys.grid.item.menu.delete.success",
						key.getName()));
			} catch (RuntimeException e) {
				LOG.error("Could not delete SSH key . ", e);
				showErrorNotification(getTranslation(
						"view.user-settings.ssh-keys.form.error.unexpected", "delete"));
			} finally {
				loadGridContent();
			}
		});
		cancelDialog.open();
	}

	private void loadGridContent() {
		grid.setItems(loadSSHKeysViewsModels());
	}

	private List<SSHKeyViewModel> loadSSHKeysViewsModels() {
		return handleExceptions(() -> sshKeysService.findOwned()).orElseGet(Collections::emptySet).stream()
				.map(key -> SSHKeyViewModelMapper.map(key, zoneId)).flatMap(List::stream)
				.sorted(comparing(sshKeyModel -> sshKeyModel.getName().toLowerCase()))
				.collect(toList());
	}
}
