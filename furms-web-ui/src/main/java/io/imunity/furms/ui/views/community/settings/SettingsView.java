/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.settings;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.function.Function.identity;

import java.util.Optional;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.ui.community.CommunityFormComponent;
import io.imunity.furms.ui.community.CommunityViewModel;
import io.imunity.furms.ui.community.CommunityViewModelMapper;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsSelectReloader;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

@Route(value = "community/admin/settings", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.settings.page.title")
public class SettingsView extends FurmsViewComponent {
	private final Binder<CommunityViewModel> binder = new BeanValidationBinder<>(CommunityViewModel.class);
	private final FormButtons formButtons;

	private final CommunityService communityService;
	private final CommunityFormComponent communityFormComponent;

	private CommunityViewModel oldCommunity;

	public SettingsView(CommunityService communityService) {
		this.communityService = communityService;
		this.communityFormComponent = new CommunityFormComponent(binder);
		this.formButtons = new FormButtons(createCancelButton(), createUpdateButton());

		communityFormComponent.getUpload().addFinishedListener(x -> enableEditorMode());
		communityFormComponent.getUpload().addFileRemovedListener(x -> enableEditorMode());
		Optional<CommunityViewModel> communityViewModel = getCommunityViewModel();
		if (communityViewModel.isPresent()){
			oldCommunity = communityViewModel.get();
			communityFormComponent.setFormPools(new CommunityViewModel(oldCommunity));
			disableEditorMode();
		}

		binder.addStatusChangeListener(status -> {
			if (oldCommunity.equalsFields(binder.getBean()))
				disableEditorMode();
			else
				enableEditorMode();
		});

		getContent().add(communityFormComponent, formButtons);
	}

	private void enableEditorMode() {
		formButtons.setVisible(true);
		formButtons.setEnabled(binder.isValid());
	}

	private void disableEditorMode() {
		formButtons.setVisible(false);
	}

	private Button createCancelButton() {
		Button closeButton = new Button(getTranslation("view.community-admin.settings.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickListener(event ->{
			loadCommunity();
			formButtons.setVisible(false);
		});
		return closeButton;
	}

	private void loadCommunity() {
		getCommunityViewModel()
			.ifPresent(communityFormComponent::setFormPools);
	}

	private Optional<CommunityViewModel> getCommunityViewModel() {
		return handleExceptions(() -> communityService.findById(getCurrentResourceId()))
			.flatMap(identity())
			.map(CommunityViewModelMapper::map);
	}

	private Button createUpdateButton() {
		Button updateButton = new Button(getTranslation("view.community-admin.settings.button.update"));
		updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		updateButton.addClickListener(x -> {
			binder.validate();
			if (binder.isValid()) {
				CommunityViewModel communityViewModel = new CommunityViewModel(binder.getBean());
				Community community = CommunityViewModelMapper.map(communityViewModel);
				getResultOrException(() -> communityService.update(community))
					.getThrowable()
					.ifPresentOrElse(
						e -> showErrorNotification(getTranslation("name.duplicated.error.message")),
						() -> {
							oldCommunity = communityViewModel;
							disableEditorMode();
							UI.getCurrent().getSession().getAttribute(FurmsSelectReloader.class).reload();
							showSuccessNotification(getTranslation("view.community-admin.settings.update.success"));
						}
					);
			}
		});
		return updateButton;
	}
}
