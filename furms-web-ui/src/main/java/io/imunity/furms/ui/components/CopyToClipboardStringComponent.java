/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import org.vaadin.olli.ClipboardHelper;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE;
import static com.vaadin.flow.component.icon.VaadinIcon.CLIPBOARD;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;

public class CopyToClipboardStringComponent
		extends HorizontalLayout
		implements HasValue<CopyToClipboardStringComponent, String>, HasValue.ValueChangeEvent<String> {

	private final Label valueLabel;
	private final ClipboardHelper clipboardButton;

	public CopyToClipboardStringComponent(String value, String onSuccessMessage) {

		this.valueLabel = new Label(value);

		Button button = new Button(CLIPBOARD.create());
		button.addThemeVariants(LUMO_TERTIARY_INLINE);
		button.addClickListener(e -> showSuccessNotification(onSuccessMessage));

		this.clipboardButton = new ClipboardHelper(value, button);

		add(this.valueLabel, this.clipboardButton);
	}

	@Override
	public void setValue(String value) {
		String oldValue = getValue();
		this.valueLabel.setText(value);
		this.clipboardButton.setContent(value);

		ComponentUtil.fireEvent(this,
				new AbstractField.ComponentValueChangeEvent<>(this, this, oldValue, true));
	}

	@Override
	public HasValue<?, String> getHasValue() {
		return this;
	}

	@Override
	public boolean isFromClient() {
		return false;
	}

	@Override
	public String getOldValue() {
		return null;
	}

	@Override
	public String getValue() {
		return this.valueLabel.getText();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super CopyToClipboardStringComponent> listener) {
		ComponentEventListener componentListener = event -> listener.valueChanged(this);
		return ComponentUtil.addListener(this, AbstractField.ComponentValueChangeEvent.class, componentListener);
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.clipboardButton.setVisible(readOnly);
	}

	@Override
	public boolean isReadOnly() {
		return !this.clipboardButton.isVisible();
	}

	@Override
	public void setRequiredIndicatorVisible(boolean b) {
		//Not implemented
	}

	@Override
	public boolean isRequiredIndicatorVisible() {
		return false;
	}
}
