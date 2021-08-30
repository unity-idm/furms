/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.invitations;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.views.site.policy_documents.PolicyDocumentFormView;
import io.imunity.furms.ui.views.user_settings.UserSettingsMenu;

import java.util.HashMap;
import java.util.Map;

import static com.vaadin.flow.component.icon.VaadinIcon.CHECK_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.CLOSE_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;

@Route(value = "users/settings/invitations", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.profile.page.title")
public class InvitationsView extends FurmsViewComponent {

	InvitationsView() {
		Grid<InvitationGridModel> grid = new SparseGrid<>(InvitationGridModel.class);
		Map<String, Checkbox> checkboxes = new HashMap<>();

		grid.addComponentColumn(x -> {
			Checkbox checkbox = new Checkbox();
			checkboxes.put(x.id ,checkbox);
			return checkbox;
		});
		grid.addColumn(x -> x.resourceName + " " + x.role)
			.setHeader(getTranslation("view.user-settings.sites.grid.title.siteName"))
			.setSortable(true);
		grid.addColumn(x -> x.originator)
			.setHeader(getTranslation("view.user-settings.sites.grid.title.siteName"))
			.setSortable(true);
		grid.addColumn(x -> x.expiration)
			.setHeader(getTranslation("view.user-settings.sites.grid.title.siteName"))
			.setSortable(true);
		grid.addComponentColumn(x -> new ContextMenu().getTarget())
			.setHeader(getTranslation("view.user-settings.sites.grid.title.siteName"))
			.setSortable(true);
	}

	private Component createContextMenu(PolicyId policyDocumentId, String policyDocumentName, String siteId) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.policy-documents.menu.edit"), CHECK_CIRCLE),
			event -> UI.getCurrent().navigate(PolicyDocumentFormView.class, policyDocumentId.id.toString())
		);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.policy-documents.menu.edit"), CLOSE_CIRCLE),
			event -> UI.getCurrent().navigate(PolicyDocumentFormView.class, policyDocumentId.id.toString())
		);

		Dialog confirmDialog = createConfirmDialog(policyDocumentId, policyDocumentName, siteId);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.policy-documents.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(PolicyId policyDocumentId, String policyDocumentName, String siteId) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.site-admin.policy-documents.dialog.text", policyDocumentName));
		furmsDialog.addConfirmButtonClickListener(event -> {
//			handleExceptions(() -> policyDocumentService.delete(siteId, policyDocumentId));
//			loadGridContent();
		});
		return furmsDialog;
	}
}
