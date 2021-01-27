/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.FileRejectedEvent;
import com.vaadin.flow.component.upload.FinishedEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import io.imunity.furms.domain.images.FurmsImage;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@CssImport("./styles/components/furms-image-upload.css")
public class FurmsImageUpload extends VerticalLayout
		implements HasValue<FurmsImageUpload, FurmsImage>, HasValue.ValueChangeEvent<FurmsImage> {

	private final static int MAX_IMAGE_SIZE_BYTES = 100000000;
	private final static String[] ACCEPTED_IMG_FILES = {"image/jpeg", "image/png", "image/gif"};

	private final Image image;
	private final Upload upload;

	private boolean readOnly;

	private MemoryBuffer memoryBuffer;
	private FurmsImage value;
	private FurmsImage oldValue;

	public FurmsImageUpload() {
		readOnly = false;
		memoryBuffer = new MemoryBuffer();
		upload = new Upload(memoryBuffer);
		upload.setAcceptedFileTypes(ACCEPTED_IMG_FILES);
		upload.setMaxFileSize(MAX_IMAGE_SIZE_BYTES);
		upload.setDropAllowed(true);
		upload.setVisible(!readOnly);

		image = new Image();
		image.setVisible(false);
		image.setId("logo");

		addFinishedListener(event -> setValueAndFireEventChange(
				new FurmsImage(image.getSrc().getBytes(), createMimeType(event.getMIMEType()))));
		addFileRemovedListener(event -> setValueAndFireEventChange(null));

		setClassName("no-left-padding");
		HorizontalLayout horizontalLayout = new HorizontalLayout(image, upload);
		horizontalLayout.setClassName("no-top-margin");
		Label logo = new Label(getTranslation("logo"));
		logo.setClassName("furms-upload-label");
		add(logo, horizontalLayout);
	}

	public void clean() {
		upload.getElement().setPropertyJson("files", Json.createArray());
	}

	public void addFinishedListener(ComponentEventListener<FinishedEvent> finishedListener) {
		upload.addFinishedListener(finishedListener);
	}

	public void addFileRejectedListener(ComponentEventListener<FileRejectedEvent> fileRejectedEvent) {
		upload.addFileRejectedListener(fileRejectedEvent);
	}

	public void addFileRemovedListener(DomEventListener fileRemovedListener) {
		upload.getElement().addEventListener("file-remove", fileRemovedListener);
	}

	public Image getImage() {
		return image;
	}

	public Upload getUpload() {
		return upload;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		upload.setVisible(!this.readOnly);
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		//Not implemented
	}

	@Override
	public boolean isRequiredIndicatorVisible() {
		return false;
	}

	public MemoryBuffer getMemoryBuffer() {
		return memoryBuffer;
	}

	@Override
	public HasValue<?, FurmsImage> getHasValue() {
		return this;
	}

	@Override
	public boolean isFromClient() {
		return false;
	}

	@Override
	public FurmsImage getOldValue() {
		return oldValue;
	}

	@Override
	public FurmsImage getValue() {
		return value;
	}

	@Override
	public void setValue(FurmsImage value) {
		this.oldValue = this.value;
		this.value = value;

		if (this.value != null) {
			this.image.setSrc(new StreamResource("", () -> new ByteArrayInputStream(this.value.getImage())));
			this.image.setVisible(!ArrayUtils.isEmpty(value.getImage()));
		}
	}

	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super FurmsImageUpload> listener) {
		ComponentEventListener componentListener = event -> listener.valueChanged(this);
		return ComponentUtil.addListener(this, AbstractField.ComponentValueChangeEvent.class, componentListener);
	}

	public FurmsImage loadFile(String mimeType) throws IOException {
		return new FurmsImage(memoryBuffer.getInputStream().readAllBytes(), createMimeType(mimeType));
	}

	private void setValueAndFireEventChange(FurmsImage newImage) {
		FurmsImage oldValueBuffered = value;
		setValue(newImage);
		ComponentUtil.fireEvent(this, new AbstractField.ComponentValueChangeEvent<>(this, this, oldValueBuffered, false));
	}

	private String createMimeType(String mimeType) {
		if (mimeType != null) {
			String[] parts = mimeType.split("/");
			if (parts.length > 1) {
				return parts[1];
			}
		}
		return "";
	}

}
