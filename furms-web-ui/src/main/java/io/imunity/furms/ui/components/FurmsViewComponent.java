/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;

import java.util.Optional;


public abstract class FurmsViewComponent extends Composite<Div> implements HasUrlParameter<String>, HasDynamicTitle {

	public FurmsViewComponent() {
		getContent().setClassName("furms-view");
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

	protected SerializablePredicate<? super String> getNotEmptyStringValidator() {
		return value -> value != null && !value.isBlank();
	}

	protected void addPreventionForMultiEnterClick() {
		Shortcuts.addShortcutListener(getContent(), event -> {}, Key.ENTER);
	}

	protected FurmsViewUserContext getActualViewUserContext() {
		Object attribute = UI.getCurrent().getSession().getAttribute(FurmsViewUserContext.class.getName());
		if (!(attribute instanceof FurmsViewUserContext)) {
			throw new IllegalArgumentException("Incorrect Furms View User Context instance.");
		}
		return (FurmsViewUserContext) attribute;
	}
}
