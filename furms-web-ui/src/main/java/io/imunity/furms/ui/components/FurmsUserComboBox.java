/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import io.imunity.furms.ui.user_context.FurmsViewUserModel;

public class FurmsUserComboBox extends VerticalLayout {
	public final ComboBox<FurmsViewUserModel>  comboBox = new ComboBox<>();

	public FurmsUserComboBox(List<FurmsViewUserModel> userModels) {
		comboBox.setItemLabelGenerator(x -> x.firstname + " " + x.lastname);
		comboBox.setItems(userModels);
		Label emailLabel = new Label();
		comboBox.addValueChangeListener(event -> Optional.ofNullable(event.getValue())
			.ifPresentOrElse(
				value -> emailLabel.setText(value.email),
				() -> emailLabel.setText(null))
		);
		setSpacing(false);
		setMargin(false);
		getStyle().set("padding", "unset");
		add(comboBox, emailLabel);
	}
}
