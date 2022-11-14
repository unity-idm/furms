/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.community;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.ui.components.FormTextArea;
import io.imunity.furms.ui.components.FormTextField;
import io.imunity.furms.ui.components.FurmsImageUpload;
import io.imunity.furms.ui.components.IdFormItem;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition.TOP;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static java.util.Optional.ofNullable;

public class CommunityFormComponent extends Composite<Div> {
	private static final int MAX_NAME_LENGTH = 20;
	private static final int MAX_DESCRIPTION_LENGTH = 510;

	private final Binder<CommunityViewModel> binder;
	private final FurmsImageUpload upload;

	private final IdFormItem idFormItem;

	public CommunityFormComponent(Binder<CommunityViewModel> binder) {
		this.binder = binder;
		this.upload = createUploadComponent();

		FormLayout formLayout = new FormLayout();

		idFormItem = new IdFormItem(getTranslation("view.fenix-admin.community.form.field.furms-id"));
		idFormItem.setVisible(false);
		formLayout.add(idFormItem);

		TextField name = new FormTextField();
		name.setValueChangeMode(EAGER);
		name.setMaxLength(MAX_NAME_LENGTH);
		formLayout.addFormItem(name, getTranslation("view.fenix-admin.community.form.field.name"));

		TextArea description = new FormTextArea();
		description.setClassName("description-text-area");
		description.setValueChangeMode(EAGER);
		description.setMaxLength(MAX_DESCRIPTION_LENGTH);
		formLayout.addFormItem(description, getTranslation("view.fenix-admin.community.form.field.description"));

		formLayout.addFormItem(upload, getTranslation("view.fenix-admin.community.form.field.logo"));
		formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("1em", 1, TOP));

		prepareBinder(binder, name, description);

		formLayout.setSizeFull();
		getContent().add(formLayout);
	}

	private void prepareBinder(Binder<CommunityViewModel> binder, TextField name, TextArea description) {
		binder.forField(name)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.fenix-admin.community.form.error.validation.field.name")
			)
			.bind(CommunityViewModel::getName, CommunityViewModel::setName);
		binder.forField(description)
			.bind(CommunityViewModel::getDescription, CommunityViewModel::setDescription);
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
			showErrorNotification(getTranslation("view.fenix-admin.community.form.error.validation.file"))
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
		idFormItem.setIdAndShow(ofNullable(communityViewModel.getId())
			.flatMap(id -> ofNullable(id.id))
			.map(UUID::toString)
			.orElse(null)
		);
	}

	public FurmsImageUpload getUpload() {
		return upload;
	}
}
