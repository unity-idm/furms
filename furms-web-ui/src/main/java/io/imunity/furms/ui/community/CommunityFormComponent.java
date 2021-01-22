/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.community;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;
import io.imunity.furms.domain.images.FurmsImage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

public class CommunityFormComponent extends Composite<Div> {
	private final static int MAX_IMAGE_SIZE_BYTES = 100000000;
	private final static String[] ACCEPTED_IMG_FILES = {"image/jpeg", "image/png", "image/gif"};
	private final Binder<CommunityViewModel> binder;
	private final Image image = new Image();

	private FurmsImage logo = new FurmsImage(null, (String) null);


	public CommunityFormComponent(Binder<CommunityViewModel> binder) {
		this.binder = binder;
		TextField name = new TextField(getTranslation("view.fenix-admin.community.form.field.name"));
		TextArea description = new TextArea(getTranslation("view.fenix-admin.community.form.field.description"));
		description.setClassName("description-text-area");
		Upload upload = createUploadComponent();

		binder.forField(name)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank() && value.length() <= 255,
				getTranslation("view.fenix-admin.community.form.error.validation.field.name")
			)
			.bind(CommunityViewModel::getName, CommunityViewModel::setName);
		binder.forField(description)
			.withValidator(
				value -> Objects.isNull(value) || value.length() <= 510,
				getTranslation("view.fenix-admin.community.form.error.validation.field.description")
			)
			.bind(CommunityViewModel::getDescription, CommunityViewModel::setDescription);

		VerticalLayout verticalLayout = new VerticalLayout(name, description);
		verticalLayout.setClassName("no-left-padding");

		HorizontalLayout horizontalLayout = new HorizontalLayout(image, upload);
		horizontalLayout.setClassName("furms-upload-layout");
		image.setId("community-logo");
		getContent().add(verticalLayout, horizontalLayout);
	}

	private Upload createUploadComponent() {
		MemoryBuffer memoryBuffer = new MemoryBuffer();
		Upload upload = new Upload(memoryBuffer);
		upload.setAcceptedFileTypes(ACCEPTED_IMG_FILES);
		upload.setMaxFileSize(MAX_IMAGE_SIZE_BYTES);
		upload.setDropAllowed(true);
		upload.addFinishedListener(event -> {
			logo = loadFile(memoryBuffer, event.getMIMEType());
			InputStream inputStream = memoryBuffer.getInputStream();
			StreamResource streamResource = new StreamResource(event.getFileName(), () -> inputStream);
			image.setSrc(streamResource);
			image.setVisible(true);
		});
		upload.addFileRejectedListener(event ->
			Notification.show(getTranslation("view.fenix-admin.community.form.error.validation.file"))
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

	public FurmsImage getLogo(){
		return logo;
	}

	public void setFormPools(CommunityViewModel communityViewModel) {
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
}
