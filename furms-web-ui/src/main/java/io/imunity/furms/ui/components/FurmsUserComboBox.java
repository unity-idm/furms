/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;

import java.util.List;
import java.util.Optional;

public class FurmsUserComboBox extends Div {
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
		getStyle().set("display", "inline-grid");
		add(comboBox, emailLabel);
	}
}
