/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.FileRejectedEvent;
import com.vaadin.flow.component.upload.FinishedEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import io.imunity.furms.domain.policy_documents.PolicyFile;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;

public class PolicyFileUpload extends HorizontalLayout
		implements HasValue<PolicyFileUpload, PolicyFile>, HasValue.ValueChangeEvent<PolicyFile> {

	private final static int MAX_FILE_SIZE_BYTES = 100000000;
	private final static String[] ACCEPTED_FILE_TYPES = {"application/pdf"};

	private final Anchor downloadIcon;
	private final Upload upload;

	private boolean readOnly;

	private MemoryBuffer memoryBuffer;
	private PolicyFile value;
	private PolicyFile oldValue;

	public PolicyFileUpload() {
		readOnly = false;
		memoryBuffer = new MemoryBuffer();
		upload = new Upload(memoryBuffer);
		upload.setAcceptedFileTypes(ACCEPTED_FILE_TYPES);
		upload.setMaxFileSize(MAX_FILE_SIZE_BYTES);
		upload.setDropAllowed(true);
		upload.setVisible(!readOnly);

		downloadIcon = new Anchor();
		downloadIcon.getElement().setAttribute("download", true);
		Button button = new Button(new Icon(VaadinIcon.FILE_TEXT));
		button.getStyle().set("margin", "0");
		button.setSizeFull();
		downloadIcon.add(button);

		addFinishedListener(event -> {
			try {
				setValue(new PolicyFile(memoryBuffer.getInputStream().readAllBytes(), createMimeType(event.getMIMEType()), event.getFileName()));
			} catch (IOException e) {
				showErrorNotification(getTranslation("file.invalidate.message"));
				throw new RuntimeException(e);
			}
		});
		addFileRemovedListener(event -> setValue(null));
		addFileRejectedListener(event ->
			showErrorNotification(getTranslation("file.invalidate.message"))
		);
		add(downloadIcon, upload);
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

		if (this.value != null) {
			downloadIcon.setHref(new StreamResource(
				value.getName() + "." + value.getTypeExtension(),
				() -> new ByteArrayInputStream(this.value.getFile()))
			);
			downloadIcon.setVisible(!ArrayUtils.isEmpty(value.getFile()));
		}
		else
			downloadIcon.setVisible(false);
		ComponentUtil.fireEvent(this, new AbstractField.ComponentValueChangeEvent<>(this, this, value, false));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super PolicyFileUpload> listener) {
		ComponentEventListener componentListener = event -> listener.valueChanged(this);
		return ComponentUtil.addListener(this, AbstractField.ComponentValueChangeEvent.class, componentListener);
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
