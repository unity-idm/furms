/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;

import static com.vaadin.flow.component.grid.ColumnTextAlign.END;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOWN;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.REFRESH;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.ADD;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.REMOVE;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.DONE;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.FAILED;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.ssh_keys.SSHKeyOperationService;
import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperation;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;
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
public class SSHKeysView extends FurmsViewComponent implements AfterNavigationObserver {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SSHKeyService sshKeysService;
	private final SSHKeyOperationService sshKeyInstallationService;
	private final Grid<SSHKeyViewModel> grid;
	private final SiteComboBoxModelResolver resolver;
	private ZoneId zoneId;

	SSHKeysView(SSHKeyService sshKeysService, AuthzService authzService, SiteService siteService,
			SSHKeyOperationService sshKeyInstallationService) {
		this.sshKeysService = sshKeysService;
		this.sshKeyInstallationService = sshKeyInstallationService;
		this.grid = createSSHKeysGrid();
		this.resolver = new SiteComboBoxModelResolver(
				siteService.findUserSites(authzService.getCurrentUserId()));

		UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
			zoneId = ZoneId.of(extendedClientDetails.getTimeZoneId());
		});

		Button addButton = createAddButton();
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

		grid.addComponentColumn(k -> gridNameComponent(grid, k))
				.setHeader(getTranslation("view.user-settings.ssh-keys.grid.column.name"))
				.setSortable(true).setComparator(x -> x.name.toLowerCase()).setResizable(true)
				.setFlexGrow(1);

		grid.addColumn(k -> SSHKey.getKeyFingerprint(k.value))
				.setHeader(getTranslation("view.user-settings.ssh-keys.grid.column.fingerprint"))
				.setSortable(true).setResizable(true).setFlexGrow(10);

		grid.addColumn(k -> k.createTime.toLocalDate())
				.setHeader(getTranslation("view.user-settings.ssh-keys.grid.column.createTime"))
				.setSortable(true).setFlexGrow(1);

		grid.addComponentColumn(k -> createLastColumnContent(k, grid))
				.setHeader(getTranslation("view.user-settings.ssh-keys.grid.column.actions"))
				.setKey("actions").setTextAlign(END);

		grid.setItemDetailsRenderer(new ComponentRenderer<>(this::additionalInfoComponent));
		grid.setSelectionMode(SelectionMode.NONE);
		return grid;
	}

	private Component gridNameComponent(Grid<SSHKeyViewModel> grid, SSHKeyViewModel item) {
		Icon icon = grid.isDetailsVisible(item) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create();
		Component routerLink;
		if (item.sites.stream().filter(s -> s.keyOperationStatus.inProgress()).findAny().isEmpty()) {
			routerLink = new RouterLink(item.name, SSHKeyFormView.class, item.id);
		} else {
			routerLink = new NoWrapLabel(item.name);
		}

		return new Div(icon, routerLink);
	}

	private Component additionalInfoComponent(SSHKeyViewModel sshKey) {
		VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.setSpacing(false);
		HorizontalLayout formLayout = new HorizontalLayout();
		formLayout.setSpacing(false);
		formLayout.setMargin(false);
		layout.add(new NoWrapLabel(getTranslation("view.user-settings.ssh-keys.grid.details.status")));
		layout.add(formLayout);
		sshKey.sites.stream().sorted((s1, s2) -> resolver.getName(s1.id).compareTo(resolver.getName(s2.id)))
				.forEach(s -> formLayout.add(new NoWrapLabel(resolver.getName(s.id) + ": "
						+ mapToStatus(s.keyOperation, s.keyOperationStatus, s.error))));
		VerticalLayout wrap = new VerticalLayout(layout);
		wrap.setSpacing(false);
		wrap.setPadding(true);
		wrap.getStyle().set("padding-top", "0px");
		return wrap;
	}

	private SSHKeyOperationJob getKeyStatus(String sshKey, String site) {
		try {
			return sshKeyInstallationService.findBySSHKeyIdAndSiteId(sshKey, site);

		} catch (Exception e) {
			LOG.error("Can not get key opertation for key {} and site {}", sshKey, site);
			return null;
		}
	}

	private String mapToStatus(SSHKeyOperation operation, SSHKeyOperationStatus status, Optional<String> error) {
		if (operation.equals(ADD)) {
			return toStatusMessage(status, "added", "adding", "error", error);

		} else if (operation.equals(REMOVE)) {

			return toStatusMessage(status, "removed", "removing", "error", error);

		} else {
			return toStatusMessage(status, "updated", "updating", "error", error);

		}
	}

	private String toStatusMessage(SSHKeyOperationStatus status, String donePostfix, String inProgressPostfix,
			String errorPostfix, Optional<String> error) {
		if (status.equals(DONE)) {
			return getTranslation("view.user-settings.ssh-keys.grid.key." + donePostfix);
		} else if (status.equals(FAILED)) {
			return getTranslation("view.user-settings.ssh-keys.grid.key." + errorPostfix);
		} else {
			return getTranslation("view.user-settings.ssh-keys.grid.key." + inProgressPostfix);
		}
	}

	private Component createLastColumnContent(SSHKeyViewModel key, Grid<SSHKeyViewModel> grid) {
		return new GridActionsButtonLayout(
				createContextMenu(key, grid)
		);
	}

	private Component createContextMenu(SSHKeyViewModel key, Grid<SSHKeyViewModel> grid) {
		GridActionMenu contextMenu = new GridActionMenu();
		contextMenu.setId(key.id);

		if (key.sites.stream().filter(s -> s.keyOperationStatus.inProgress()).findAny().isEmpty()) {
			contextMenu.addItem(new MenuButton(getTranslation("view.sites.main.grid.item.menu.edit"), EDIT),
					event -> UI.getCurrent().navigate(SSHKeyFormView.class, key.id));
			contextMenu.addItem(
					new MenuButton(getTranslation("view.user-settings.ssh-keys.grid.menu.delete"),
							TRASH),
					e -> actionDeleteSSHKey(key, grid));
		}
		contextMenu.addItem(new MenuButton(getTranslation("view.user-settings.ssh-keys.grid.menu.refresh"),
				REFRESH), e -> refreshDetails(key));

		getContent().add(contextMenu);
		Component target = contextMenu.getTarget();
		target.setVisible(contextMenu.isVisible());
		
		return target;
	}

	private void actionDeleteSSHKey(SSHKeyViewModel key, Grid<SSHKeyViewModel> grid) {
		FurmsDialog cancelDialog = new FurmsDialog(getTranslation(
				"view.user-settings.ssh-keys.main.confirmation.dialog.delete", key.name));
		cancelDialog.addConfirmButtonClickListener(event -> {
			try {
				sshKeysService.delete(key.id);
				showSuccessNotification(getTranslation(
						"view.user-settings.ssh-keys.grid.item.menu.delete.success", key.name));
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

	private void refreshDetails(SSHKeyViewModel key) {
		boolean details = grid.isDetailsVisible(key);
		List<SSHKeyViewModel> models = loadSSHKeysViewsModels();
		grid.setItems(models);
		Optional<SSHKeyViewModel> selectedModel = models.stream().filter(m -> m.id.equals(key.id)).findAny();
		if (selectedModel.isPresent()) {
			grid.setDetailsVisible(selectedModel.get(), details);
		}
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {

		try {
			sshKeysService.assertIsEligibleToManageKeys();
		} catch (UserWithoutFenixIdValidationError e) {
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("user.without.fenixid.error.message"));
			setVisible(false);
			return;
		} catch (AccessDeniedException e) {
			LOG.error(e.getMessage(), e);
			showErrorNotification(
					getTranslation("view.user-settings.ssh-keys.access.denied.error.message"));
			setVisible(false);
			return;
		}

		loadGridContent();
	}

	private List<SSHKeyViewModel> loadSSHKeysViewsModels() {
		try {
			setVisible(true);
			return sshKeysService.findOwned().stream().map(
					key -> SSHKeyViewModelMapper.map(key, zoneId, (k, s) -> getKeyStatus(k, s)))
					.sorted(comparing(sshKeyModel -> sshKeyModel.name.toLowerCase()))
					.collect(toList());
		} catch (AccessDeniedException e) {
			LOG.error(e.getMessage(), e);
			showErrorNotification(
					getTranslation("view.user-settings.ssh-keys.access.denied.error.message"));
			setVisible(false);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("base.error.message"));
		}

		return Collections.emptyList();
	}

	static class NoWrapLabel extends Label {
		NoWrapLabel(String text) {
			super(text);
			getStyle().set("white-space", "nowrap");
		}
	}

}
