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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.FileRejectedEvent;
import com.vaadin.flow.component.upload.FinishedEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import io.imunity.furms.domain.policy_documents.PolicyFile;

import java.io.IOException;

@CssImport("./styles/components/furms-image-upload.css")
public class PolicyFileUpload extends HorizontalLayout
		implements HasValue<PolicyFileUpload, PolicyFile>, HasValue.ValueChangeEvent<PolicyFile> {

	private final static int MAX_IMAGE_SIZE_BYTES = 100000000;
	private final static String[] ACCEPTED_IMG_FILES = {"image/jpeg", "image/png", "image/gif"};

	private final Upload upload;

	private boolean readOnly;

	private MemoryBuffer memoryBuffer;
	private PolicyFile value;
	private PolicyFile oldValue;

	public PolicyFileUpload() {
		readOnly = false;
		memoryBuffer = new MemoryBuffer();
		upload = new Upload(memoryBuffer);
		upload.setAcceptedFileTypes(ACCEPTED_IMG_FILES);
		upload.setMaxFileSize(MAX_IMAGE_SIZE_BYTES);
		upload.setDropAllowed(true);
		upload.setVisible(!readOnly);

//		addFinishedListener(event -> setValueAndFireEventChange(
//				new FurmsImage(image.getSrc().getBytes(), createMimeType(event.getMIMEType()))));
//		addFileRemovedListener(event -> setValueAndFireEventChange(null));

		add(upload);
	}

	public void cleanCurrentFileName() {
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

//	public Image getImage() {
//		return image;
//	}

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
	public HasValue<?, PolicyFile> getHasValue() {
		return this;
	}

	@Override
	public boolean isFromClient() {
		return false;
	}

	@Override
	public PolicyFile getOldValue() {
		return oldValue;
	}

	@Override
	public PolicyFile getValue() {
		return value;
	}

	@Override
	public void setValue(PolicyFile value) {
		this.oldValue = this.value;
		this.value = value;

//		if (this.value != null) {
//			this.image.setSrc(new StreamResource("", () -> new ByteArrayInputStream(this.value.getImage())));
//			this.image.setVisible(!ArrayUtils.isEmpty(value.getImage()));
//		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super PolicyFileUpload> listener) {
		ComponentEventListener componentListener = event -> listener.valueChanged(this);
		return ComponentUtil.addListener(this, AbstractField.ComponentValueChangeEvent.class, componentListener);
	}

	public PolicyFile loadFile(String mimeType) throws IOException {
		return new PolicyFile(memoryBuffer.getInputStream().readAllBytes(), createMimeType(mimeType));
	}

	private void setValueAndFireEventChange(PolicyFile newImage) {
		PolicyFile oldValueBuffered = value;
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
