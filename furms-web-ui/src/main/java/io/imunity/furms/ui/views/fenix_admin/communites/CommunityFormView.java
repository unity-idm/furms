/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.communites;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.ui.config.FrontProperties;
import io.imunity.furms.ui.views.components.BreadCrumbParameter;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.fenix_admin.communites.model.CommunityViewModel;
import io.imunity.furms.ui.views.fenix_admin.communites.model.CommunityViewModelMapper;
import io.imunity.furms.ui.views.fenix_admin.menu.FenixAdminMenu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Route(value = "fenix/admin/communities/form", layout = FenixAdminMenu.class)
@PageTitle(key = "view.community.form.page.title")
class CommunityFormView extends FurmsViewComponent {
	private final static int hundredMB = 100000000;
	private final Binder<CommunityViewModel> binder = new BeanValidationBinder<>(CommunityViewModel.class);
	private final Image image = new Image();
	private final CommunityService communityService;
	private final String[] acceptedImgFiles;

	private BreadCrumbParameter breadCrumbParameter;
	private FurmsImage logo = new FurmsImage(null, (String) null);

	CommunityFormView(CommunityService communityService, FrontProperties frontProperties) {
		this.communityService = communityService;
		this.acceptedImgFiles = frontProperties.getAcceptedImgFiles().toArray(String[]::new);

		TextField name = new TextField(getTranslation("view.community.form.field.name"));
		TextArea description = new TextArea(getTranslation("view.community.form.field.description"));
		description.setClassName("description-text-area");
		Button saveButton = createSaveButton();
		Button closeButton = createCloseButton();
		Upload upload = createUploadComponent();

		binder.forField(name)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank() && value.length() <= 255,
				getTranslation("view.community.form.error.validation.field.name")
			)
			.bind(CommunityViewModel::getName, CommunityViewModel::setName);
		binder.forField(description)
			.withValidator(
				value -> Objects.isNull(value) || value.length() <= 510,
				getTranslation("view.community.form.error.validation.field.description")
			)
			.bind(CommunityViewModel::getDescription, CommunityViewModel::setDescription);
		binder.addStatusChangeListener(env -> binder.isValid());

		VerticalLayout verticalLayout = new VerticalLayout(name, description);
		verticalLayout.setClassName("no-left-padding");

		HorizontalLayout horizontalLayout = new HorizontalLayout(image, upload);
		horizontalLayout.setClassName("furms-upload-layout");
		image.setId("community-logo");

		getContent().add(
			verticalLayout,
			horizontalLayout,
			new HorizontalLayout(saveButton, closeButton)
		);
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.community.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(CommunitiesView.class));
		return closeButton;
	}

	private Upload createUploadComponent() {
		MemoryBuffer memoryBuffer = new MemoryBuffer();
		Upload upload = new Upload(memoryBuffer);
		upload.setAcceptedFileTypes(acceptedImgFiles);
		upload.setMaxFileSize(hundredMB);
		upload.setDropAllowed(true);
		upload.addFinishedListener(event -> {
			logo = loadFile(memoryBuffer, event.getMIMEType());
			InputStream inputStream = memoryBuffer.getInputStream();
			StreamResource streamResource = new StreamResource(event.getFileName(), () -> inputStream);
			image.setSrc(streamResource);
			image.setVisible(true);
		});
		upload.addFileRejectedListener(event ->
			Notification.show(getTranslation("view.community.form.error.validation.file"))
		);
		upload.getElement().addEventListener("file-remove", event -> {
			logo = new FurmsImage(null, (String)null);
			image.setVisible(false);
		});
		return upload;
	}

	private FurmsImage loadFile(MemoryBuffer memoryBuffer, String mimeType) {
		try {
			return new FurmsImage(memoryBuffer.getInputStream().readAllBytes(), mimeType.split("/")[1]);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Button createSaveButton() {
		Button saveButton = new Button(getTranslation("view.community.form.button.save"));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			CommunityViewModel communityViewModel = binder.getBean();
			communityViewModel.setLogoImage(logo);
			Community community = CommunityViewModelMapper.map(communityViewModel);
			if(community.getId() == null)
				communityService.create(community);
			else
				communityService.update(community);
			UI.getCurrent().navigate(CommunitiesView.class);
		});
		return saveButton;
	}

	private void setFormPools(CommunityViewModel communityViewModel) {
		binder.setBean(communityViewModel);
		logo = communityViewModel.getLogoImage();
		image.setVisible(logo != null && logo.getImage() != null);
		if(logo != null) {
			StreamResource resource =
				new StreamResource(
					UUID.randomUUID().toString() + "." + logo.getType(),
					() -> new ByteArrayInputStream(logo.getImage())
				);
			image.setSrc(resource);
		}
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		CommunityViewModel communityViewModel = ofNullable(parameter)
			.map(communityService::findById)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(CommunityViewModelMapper::map)
			.orElseGet(CommunityViewModel::new);
		String trans = parameter == null ? "view.community.form.parameter.new" : "view.community.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		setFormPools(communityViewModel);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}

}
