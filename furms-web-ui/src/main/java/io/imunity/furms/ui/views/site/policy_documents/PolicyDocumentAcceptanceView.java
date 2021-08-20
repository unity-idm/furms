/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.policy_documents;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UserContextMenuFactory;
import io.imunity.furms.ui.components.administrators.UserGrid;
import io.imunity.furms.ui.components.administrators.UserGridItem;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.vaadin.flow.component.icon.VaadinIcon.CHECK_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.PAPERPLANE;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Route(value = "site/admin/policy/documents/acceptance", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.policy-documents-acceptance.page.title")
public class PolicyDocumentAcceptanceView extends FurmsViewComponent {
	private final PolicyDocumentService policyDocumentService;
	private PolicyDocument policyDocument;

	private BreadCrumbParameter breadCrumbParameter;

	protected PolicyDocumentAcceptanceView(PolicyDocumentService policyDocumentService) {
		this.policyDocumentService = policyDocumentService;
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter){
		Optional<PolicyDocument> optionalPolicyDocument = policyDocumentService.findById(getCurrentResourceId(), new PolicyId(parameter));
		if(optionalPolicyDocument.isPresent()) {
			this.policyDocument = optionalPolicyDocument.get();
			this.breadCrumbParameter = new BreadCrumbParameter(
				policyDocument.id.id.toString(), policyDocument.name,
				getTranslation("view.site-admin.policy-documents-acceptance.bread-cramb"));
			fillPage();
		}
		else {
			showErrorNotification(getTranslation("view.site-admin.policy-documents-acceptance.wrong.id"));
		}
	}

	private void fillPage() {
		getContent().removeAll();
		ViewHeaderLayout viewHeaderLayout = new ViewHeaderLayout(policyDocument.name + " " + getTranslation("view.site-admin.policy-documents-acceptance.half.header"));
		UserContextMenuFactory.Builder builder = UserContextMenuFactory.builder();
		if(policyDocument.workflow.equals(PolicyWorkflow.PAPER_BASED)) {
			builder.addCustomContextMenuItem(
				x -> new MenuButton(getTranslation("view.site-admin.policy-documents-acceptance.menu.accept"), CHECK_CIRCLE),
				(UserGridItem userGridItem) -> {
					PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
						.policyDocumentId(policyDocument.id)
						.policyDocumentRevision(policyDocument.revision)
						.acceptanceStatus(PolicyAcceptanceStatus.ACCEPTED)
						.decisionTs(convertToUTCTime(ZonedDateTime.now(ZoneId.systemDefault())).toInstant(ZoneOffset.UTC))
						.build();
					policyDocumentService.addUserPolicyAcceptance(policyDocument.siteId, userGridItem.getFenixUserId().get(), policyAcceptance);
				});
		}
		builder.addCustomContextMenuItem(
				x -> new MenuButton(getTranslation("view.site-admin.policy-documents-acceptance.menu.resend"), PAPERPLANE),
				(UserGridItem userGridItem) -> policyDocumentService.resendPolicyInfo(userGridItem.getId().get(), policyDocument.id)
			);

		UserContextMenuFactory userContextMenuFactory = builder.build();
		UserGrid.Builder userGrid = UserGrid.builder()
			.withFullNameColumn()
			.withEmailColumn()
			.withContextMenuColumn(userContextMenuFactory);
		UsersGridComponent grid = UsersGridComponent.defaultInit(
			() -> policyDocumentService.findAllUsersWithoutCurrentRevisionPolicyAcceptance(policyDocument.siteId, policyDocument.id),
			userGrid
		);

		getContent().add(viewHeaderLayout, grid);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}

}
