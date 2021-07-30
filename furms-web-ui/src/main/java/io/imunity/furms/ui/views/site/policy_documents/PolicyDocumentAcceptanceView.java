/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.policy_documents;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

import static com.vaadin.flow.component.icon.VaadinIcon.CHECK_CIRCLE;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Route(value = "site/admin/policy/documents/acceptance", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.policy-documents-acceptance.page.title")
public class PolicyDocumentAcceptanceView extends FurmsViewComponent {
	private final PolicyDocumentService policyDocumentService;
	private final Grid<FURMSUser> grid;
	private final ViewHeaderLayout viewHeaderLayout;
	private PolicyDocument policyDocument;

	private BreadCrumbParameter breadCrumbParameter;

	PolicyDocumentAcceptanceView(PolicyDocumentService policyDocumentService) {
		this.policyDocumentService = policyDocumentService;
		this.grid = createUserAcceptanceGrid();
		this.viewHeaderLayout = new ViewHeaderLayout("");

		getContent().add(viewHeaderLayout, grid);
	}

	private Grid<FURMSUser> createUserAcceptanceGrid() {
		Grid<FURMSUser> grid = new SparseGrid<>(FURMSUser.class);

		grid.addColumn(model -> model.firstName.orElse(""))
			.setHeader(getTranslation("view.site-admin.policy-documents-acceptance.grid.1"))
			.setSortable(true);
		grid.addColumn(model -> model.lastName.orElse(""))
			.setHeader(getTranslation("view.site-admin.policy-documents-acceptance.grid.2"))
			.setSortable(true);
		grid.addColumn(model -> model.email)
			.setHeader(getTranslation("view.site-admin.policy-documents-acceptance.grid.3"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.site-admin.policy-documents-acceptance.grid.4"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(FURMSUser model) {
		Component contextMenu = createContextMenu(model);
		return new GridActionsButtonLayout(contextMenu);
	}

	private Component createContextMenu(FURMSUser model) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.policy-documents-acceptance.menu.accept"), CHECK_CIRCLE),
			event -> {
				PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
					.policyDocumentId(policyDocument.id)
					.policyDocumentRevision(policyDocument.revision)
					.acceptanceStatus(PolicyAcceptanceStatus.ACCEPTED)
					.decisionTs(convertToUTCTime(ZonedDateTime.now(ZoneId.systemDefault())).toInstant(ZoneOffset.UTC))
					.build();
				policyDocumentService.addUserPolicyAcceptance(policyDocument.siteId, model.fenixUserId.get(), policyAcceptance);
				loadGridContent();
			}
		);

		return contextMenu.getTarget();
	}

	private void loadGridContent() {
		grid.setItems(policyDocumentService.findAllUserWithoutPolicyAcceptance(policyDocument.siteId, policyDocument.id));
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter){
		Optional<PolicyDocument> optionalPolicyDocument = policyDocumentService.findById(getCurrentResourceId(), new PolicyId(parameter));
		if(optionalPolicyDocument.isPresent()) {
			this.policyDocument = optionalPolicyDocument.get();
			this.breadCrumbParameter = new BreadCrumbParameter(policyDocument.id.id.toString(), policyDocument.name,
				getTranslation("view.site-admin.policy-documents-acceptance.bread-cramb"));
			this.viewHeaderLayout.setText(policyDocument.name + " " + getTranslation("view.site-admin.policy-documents-acceptance.half.header"));
			loadGridContent();
		}
		else {
			showErrorNotification(getTranslation("view.site-admin.policy-documents-acceptance.wrong.id"));
		}
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}

}
