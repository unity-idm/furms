/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;

import java.util.Optional;

import static com.vaadin.flow.component.notification.Notification.Position.TOP_END;

public abstract class FurmsViewComponent extends Composite<Div> implements HasUrlParameter<String>, HasDynamicTitle {

	public FurmsViewComponent() {
		getContent().setClassName("furms-view");
		addPreventionForMultiEnterClick();
	}

	public Optional<BreadCrumbParameter> getParameter(){
		return Optional.empty();
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {}

	@Override
	public String getPageTitle() {
		return getTranslation(getClass().getAnnotation(PageTitle.class).key());
	}

	protected void showErrorNotification(String message) {
		HorizontalLayout errorLayout = new HorizontalLayout(VaadinIcon.WARNING.create(), new Label(message));
		errorLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		Notification error = new Notification(errorLayout);
		error.setDuration(5000);
		error.setPosition(TOP_END);
		error.setThemeName("error");
		error.setOpened(true);
	}

	protected SerializablePredicate<? super String> getNotEmptyStringValidator() {
		return value -> value != null && !value.isBlank();
	}

	private void addPreventionForMultiEnterClick() {
		Shortcuts.addShortcutListener(getContent(), event -> {}, Key.ENTER);
	}
}
