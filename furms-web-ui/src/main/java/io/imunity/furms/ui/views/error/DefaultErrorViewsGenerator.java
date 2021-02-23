/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.error;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import io.imunity.furms.ui.views.landing.LandingPageView;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.icon.VaadinIcon.ARROW_LEFT;
import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

class DefaultErrorViewsGenerator {

	static Element generate(String title, String message) {
		VerticalLayout error = new VerticalLayout();
		Button backButton = new Button(getTranslation("view.error-page.error.back-button"), ARROW_LEFT.create(),
				e -> UI.getCurrent().navigate(LandingPageView.class));
		backButton.addThemeVariants(LUMO_TERTIARY);

		error.add(backButton);

		VerticalLayout messageLayout = new VerticalLayout(new H2(title), new Text(message));
		messageLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		messageLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
		messageLayout.setSizeFull();

		error.add(messageLayout);

		return error.getElement();
	}

}
