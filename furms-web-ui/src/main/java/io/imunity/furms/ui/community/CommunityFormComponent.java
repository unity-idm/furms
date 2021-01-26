/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.community;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.ui.components.FurmsImageUpload;

import java.io.IOException;
import java.util.Objects;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;

public class CommunityFormComponent extends Composite<Div> {
	private final Binder<CommunityViewModel> binder;
	private final FurmsImageUpload upload;

	public CommunityFormComponent(Binder<CommunityViewModel> binder) {
		this.binder = binder;
		TextField name = new TextField(getTranslation("view.fenix-admin.community.form.field.name"));
		TextArea description = new TextArea(getTranslation("view.fenix-admin.community.form.field.description"));
		description.setClassName("description-text-area");
		upload = createUploadComponent();

		binder.forField(name)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.fenix-admin.community.form.error.validation.field.name.1")
			)
			.withValidator(
				value -> value.length() <= 20,
				getTranslation("view.fenix-admin.community.form.error.validation.field.name.2")
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

		getContent().add(verticalLayout, upload);
	}

	private FurmsImageUpload createUploadComponent() {
		FurmsImageUpload upload = new FurmsImageUpload();
		upload.addFinishedListener(event -> {
			try {
				binder.getBean().setLogoImage(upload.loadFile(event.getMIMEType()));
				StreamResource streamResource =
					new StreamResource(event.getFileName(), upload.getMemoryBuffer()::getInputStream);
				upload.getImage().setSrc(streamResource);
				upload.getImage().setVisible(true);
			} catch (IOException e) {
				showErrorNotification(getTranslation("view.site-admin.settings.form.logo.error"));
			}
		});
		upload.addFileRejectedListener(event ->
			Notification.show(getTranslation("view.fenix-admin.community.form.error.validation.file"))
		);
		upload.addFileRemovedListener(event -> {
			binder.getBean().setLogoImage(FurmsImage.empty());
			upload.getImage().setVisible(false);
		});
		return upload;
	}

	public void setFormPools(CommunityViewModel communityViewModel) {
		binder.setBean(communityViewModel);
		upload.setValue(communityViewModel.getLogoImage());
	}
}
