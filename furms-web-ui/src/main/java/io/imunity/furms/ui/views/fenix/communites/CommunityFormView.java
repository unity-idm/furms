/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.ui.community.CommunityFormComponent;
import io.imunity.furms.ui.community.CommunityViewModel;
import io.imunity.furms.ui.community.CommunityViewModelMapper;
import io.imunity.furms.ui.components.layout.BreadCrumbParameter;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.utils.OptionalException;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

import java.util.Optional;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

@Route(value = "fenix/admin/communities/form", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.community.form.page.title")
class CommunityFormView extends FurmsViewComponent {
	private final Binder<CommunityViewModel> binder = new BeanValidationBinder<>(CommunityViewModel.class);
	private final CommunityFormComponent communityFormComponent;
	private final CommunityService communityService;
	private BreadCrumbParameter breadCrumbParameter;

	CommunityFormView(CommunityService communityService) {
		this.communityService = communityService;
		this.communityFormComponent = new CommunityFormComponent(binder);

		Button saveButton = createSaveButton();
		Button cancelButton = createCloseButton();

		FormButtons buttons = new FormButtons(cancelButton, saveButton);
		getContent().add(communityFormComponent, buttons);
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.fenix-admin.community.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(CommunitiesView.class));
		return closeButton;
	}

	private Button createSaveButton() {
		Button saveButton = new Button(getTranslation("view.fenix-admin.community.form.button.save"));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid()) {
				saveCommunity();
			}
		});

		return saveButton;
	}

	private void saveCommunity() {
		CommunityViewModel communityViewModel = binder.getBean();
		Community community = CommunityViewModelMapper.map(communityViewModel);
		OptionalException<Void> optionalException;
		if (community.getId() == null)
			optionalException = getResultOrException(() -> communityService.create(community));
		else
			optionalException = getResultOrException(() -> communityService.update(community));
		optionalException.getException().ifPresentOrElse(
			throwable -> showErrorNotification(getTranslation(throwable.getMessage())),
			() -> UI.getCurrent().navigate(CommunitiesView.class)
		);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		CommunityViewModel communityViewModel = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> communityService.findById(id)))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(CommunityViewModelMapper::map)
			.orElseGet(CommunityViewModel::new);
		String trans = parameter == null ? "view.fenix-admin.community.form.parameter.new" : "view.fenix-admin.community.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		communityFormComponent.setFormPools(communityViewModel);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}

}
