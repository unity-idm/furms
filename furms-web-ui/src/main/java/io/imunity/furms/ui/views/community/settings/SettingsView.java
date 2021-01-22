/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.settings;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.community.CommunityFormComponent;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;
import io.imunity.furms.ui.community.CommunityViewModel;
import io.imunity.furms.ui.community.CommunityViewModelMapper;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.function.Function.identity;

@Route(value = "community/admin/settings", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.settings.page.title")
public class SettingsView extends FurmsViewComponent {
	private final Binder<CommunityViewModel> binder = new BeanValidationBinder<>(CommunityViewModel.class);
	private final CommunityService communityService;
	private final CommunityFormComponent communityFormComponent;

	public SettingsView(CommunityService communityService) {
		this.communityService = communityService;
		this.communityFormComponent = new CommunityFormComponent(binder);

		handleExceptions(() -> communityService.findById(getCurrentResourceId()))
			.flatMap(identity())
			.map(CommunityViewModelMapper::map)
			.ifPresent(communityFormComponent::setFormPools);

		getContent().add(
			communityFormComponent,
			createUpdateButton()
		);
	}

	private Button createUpdateButton() {
		Button updateButton = new Button(getTranslation("view.community-admin.settings.button.update"));
		updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		updateButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid()) {
				CommunityViewModel communityViewModel = binder.getBean();
				communityViewModel.setLogoImage(communityFormComponent.getLogo());
				Community community = CommunityViewModelMapper.map(communityViewModel);
				handleExceptions(() -> communityService.update(community));
			}
		});
		return updateButton;
	}
}
