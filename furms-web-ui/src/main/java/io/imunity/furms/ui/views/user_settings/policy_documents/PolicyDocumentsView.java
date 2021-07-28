/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.policy_documents;

import static com.vaadin.flow.component.icon.VaadinIcon.CHECK_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.EYE;
import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

import java.io.ByteArrayInputStream;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.StreamResource;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.domain.policy_documents.PolicyAgreement;
import io.imunity.furms.domain.policy_documents.PolicyAgreementStatus;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.IconButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.user_context.InvocationContext;
import io.imunity.furms.ui.views.user_settings.UserSettingsMenu;

@Route(value = "users/settings/policy/documents", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.policy-documents.page.title")
public class PolicyDocumentsView extends FurmsViewComponent {
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private final PolicyDocumentService policyDocumentService;

	private final Grid<PolicyDocumentExtended> grid;

	PolicyDocumentsView(AuthzService authzService, PolicyDocumentService service) {
		this.policyDocumentService = service;
		ZoneId browserZoneId = InvocationContext.getCurrent().getZone();
		ViewHeaderLayout layout = new ViewHeaderLayout(getTranslation("view.user-settings.policy-documents.page.title"));
		this.grid = new SparseGrid<>(PolicyDocumentExtended.class);
		grid.addColumn(x -> x.name)
			.setHeader(getTranslation("view.user-settings.policy-documents.grid.1"))
			.setSortable(true);
		grid.addColumn(x -> getTranslation("view.user-settings.policy-documents.workflow." + x.workflow.getPersistentId()))
			.setHeader(getTranslation("view.user-settings.policy-documents.grid.2"))
			.setSortable(true);
		grid.addColumn(x ->
				x.utcAcceptedTime
					.map(t -> convertToZoneTime(t, browserZoneId))
					.map(t -> t.format(dateTimeFormatter))
					.orElse(getTranslation("view.user-settings.policy-documents.accepted-on.null"))
		)
			.setHeader(getTranslation("view.user-settings.policy-documents.grid.3"))
			.setSortable(true);
		grid.addColumn(x -> x.siteName)
			.setHeader(getTranslation("view.user-settings.policy-documents.grid.4"))
			.setSortable(true);
		grid.addColumn(x -> x.serviceName)
			.setHeader(getTranslation("view.user-settings.policy-documents.grid.5"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.user-settings.policy-documents.grid.6"))
			.setTextAlign(ColumnTextAlign.END);

		loadGridContent();
		getContent().add(layout, grid);
	}

	private void loadGridContent() {
		Set<PolicyDocumentExtended> policies = policyDocumentService.findAllByCurrentUser();
		grid.setItems(policies.stream().sorted(Comparator.comparing(x -> x.name)));
	}

	private HorizontalLayout createLastColumnContent(PolicyDocumentExtended policyDocumentExtended) {
		Component eyeIcon = createEyeIcon(policyDocumentExtended);
		if(policyDocumentExtended.workflow.equals(PolicyWorkflow.WEB_BASED) && policyDocumentExtended.utcAcceptedTime.isEmpty()) {
			IconButton iconApproveButton = createApproveIcon(policyDocumentExtended);
			return new GridActionsButtonLayout(iconApproveButton, eyeIcon);
		}
		return new GridActionsButtonLayout(eyeIcon);
	}

	private IconButton createApproveIcon(PolicyDocumentExtended policyDocumentExtended) {
		IconButton iconApproveButton = new IconButton(CHECK_CIRCLE.create());
		iconApproveButton.addClickListener(event -> {
			PolicyAgreement policyAgreement = PolicyAgreement.builder()
				.policyDocumentId(policyDocumentExtended.id)
				.policyDocumentRevision(policyDocumentExtended.revision)
				.acceptanceStatus(PolicyAgreementStatus.ACCEPTED)
				.decisionTs(convertToUTCTime(ZonedDateTime.now(ZoneId.systemDefault())).toInstant(ZoneOffset.UTC))
				.build();
			policyDocumentService.addCurrentUserPolicyAgreement(policyAgreement);
			loadGridContent();
		});
		return iconApproveButton;
	}

	private Component createEyeIcon(PolicyDocumentExtended policyDocumentExtended) {
		Anchor anchor = new Anchor( "" ,  EYE.create());
		anchor.getStyle().set("align-self", "center");
		if(policyDocumentExtended.contentType.equals(PolicyContentType.EMBEDDED)){
			Html html = new Html("<div>" + policyDocumentExtended.htmlText + "</div>");
			anchor.setTarget( "_blank" );
			String id = UUID.randomUUID().toString();
			String url = RouteConfiguration.forSessionScope().getUrl(EmbeddedPolicyDocumentView.class, id);
			anchor.setHref(url);
			UI.getCurrent().getSession().setAttribute(id, html);
		}
		else {
			anchor.getElement().setAttribute("download", true);
			anchor.setHref(new StreamResource(policyDocumentExtended.policyFile.getName() + "." + policyDocumentExtended.policyFile.getTypeExtension(), () -> new ByteArrayInputStream(policyDocumentExtended.policyFile.getFile())));
		}
		return anchor;
	}
}