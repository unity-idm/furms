/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;

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
		return value -> value != null && !value.trim().isBlank();
	}
}
