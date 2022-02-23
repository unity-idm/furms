/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.shared.Registration;
import io.imunity.furms.ui.user_context.UIContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

/**
 * Works on dates aligned to browser's zone.
 */
@CssImport("./styles/components/date-time-picker.css")
public class FurmsDateTimePicker
		extends Div
		implements HasValue<FurmsDateTimePicker, ZonedDateTime>, HasValue.ValueChangeEvent<ZonedDateTime>, HasValidation {

	private static final Locale EUROPEAN_FORMAT_LOCALE = new Locale("DE");

	private final DatePicker datePicker;
	private final TimePicker timePicker;
	private final ToggleButton enableTimeButton;

	private ZonedDateTime oldValue;

	private final Supplier<LocalTime> defaultTimeProvider;

	public FurmsDateTimePicker(Supplier<LocalTime> defaultTimeValueProvider) {
		this.defaultTimeProvider = defaultTimeValueProvider;

		this.datePicker = new DatePicker();
		this.datePicker.setReadOnly(false);
		this.datePicker.addValueChangeListener(event -> setValueAndFireEventChange());
		this.datePicker.setLocale(EUROPEAN_FORMAT_LOCALE);
		this.timePicker = new TimePicker();
		this.timePicker.setReadOnly(false);
		this.timePicker.setVisible(false);
		this.timePicker.setLocale(EUROPEAN_FORMAT_LOCALE);
		this.timePicker.addValueChangeListener(event -> setValueAndFireEventChange());

		this.enableTimeButton = new ToggleButton(getTranslation("component.date-time-picker.toggle-button.label"));
		this.enableTimeButton.addValueChangeListener(this::toggleButtonValueChange);

		final VerticalLayout content = new VerticalLayout(
				new HorizontalLayout(this.datePicker, this.timePicker),
				this.enableTimeButton);
		content.addClassName("date-time-picker-content");

		add(content);
	}

	private void setValueAndFireEventChange() {
		ZonedDateTime oldValueBuffered = getValue();
		ComponentUtil.fireEvent(this,
				new AbstractField.ComponentValueChangeEvent<>(this, this, oldValueBuffered, true));
	}

	public void setWidth(String width) {
		datePicker.setWidth(width);
		timePicker.setWidth(width);
	}

	@Override
	public void setValue(ZonedDateTime value) {
		if (value != null) {
			this.oldValue = getValue();
			datePicker.setValue(value.toLocalDate());
			timePicker.setValue(value.toLocalTime());
		} else {
			datePicker.setValue(nowWithBrowserZone());
			timePicker.setValue(defaultTimeProvider.get());
		}

		showTimePickerIfWasAdjusted();
	}

	@Override
	public HasValue<?, ZonedDateTime> getHasValue() {
		return this;
	}

	@Override
	public boolean isFromClient() {
		return false;
	}

	@Override
	public ZonedDateTime getOldValue() {
		return oldValue;
	}

	@Override
	public ZonedDateTime getValue() {
		ZoneId browserZoneId = UIContext.getCurrent().getZone();
		return ZonedDateTime.of(
				ofNullable(datePicker.getValue()).orElse(now(browserZoneId)),
				ofNullable(timePicker.getValue()).orElse(defaultTimeProvider.get()),
				browserZoneId);
	}
	
	private LocalDate nowWithBrowserZone() {
		ZoneId browserZoneId = UIContext.getCurrent().getZone();
		return now(browserZoneId);
	}
	
	private LocalDate now(ZoneId browserZoneId) {
		return ZonedDateTime.now(browserZoneId).toLocalDate();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super FurmsDateTimePicker> listener) {
		ComponentEventListener componentListener = event -> listener.valueChanged(this);
		return ComponentUtil.addListener(this, AbstractField.ComponentValueChangeEvent.class, componentListener);
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.datePicker.setReadOnly(readOnly);
		this.timePicker.setReadOnly(readOnly);
		this.enableTimeButton.setEnabled(!readOnly);
	}

	@Override
	public boolean isReadOnly() {
		return this.datePicker.isReadOnly() && this.timePicker.isReadOnly();
	}

	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		//Not implemented
	}

	@Override
	public boolean isRequiredIndicatorVisible() {
		return false;
	}

	private void toggleButtonValueChange(AbstractField.ComponentValueChangeEvent<ToggleButton, Boolean> event) {
		final Boolean isON = enableTimeButton.getValue();
		timePicker.setVisible(isON);
		if (!isON) {
			timePicker.setValue(defaultTimeProvider.get());
		}
	}

	@Override
	public void setErrorMessage(String errorMessage) {
		this.datePicker.setErrorMessage(errorMessage);
	}

	@Override
	public String getErrorMessage() {
		return this.datePicker.getErrorMessage();
	}

	@Override
	public void setInvalid(boolean invalid) {
		this.datePicker.setInvalid(invalid);
		this.timePicker.setInvalid(invalid);
	}

	@Override
	public boolean isInvalid() {
		return this.datePicker.isInvalid() && this.datePicker.isInvalid();
	}

	private void showTimePickerIfWasAdjusted() {
		if (!timePicker.getValue().equals(defaultTimeProvider.get())) {
			enableTimeButton.setValue(true);
			timePicker.setVisible(true);
		}
	}
}
