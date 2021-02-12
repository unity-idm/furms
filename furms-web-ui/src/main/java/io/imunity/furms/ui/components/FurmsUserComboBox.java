/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@CssImport("./styles/components/furms-select.css")
public class FurmsUserComboBox extends VerticalLayout {
	public final ComboBox<FurmsViewUserModel>  comboBox = new ComboBox<>();

	public FurmsUserComboBox(List<FurmsViewUserModel> userModels) {
		comboBox.setItemLabelGenerator(user -> {
			String fullName = ofNullable(user.firstname)
				.map(value -> value + " ").orElse("")
				+ ofNullable(user.lastname).orElse("");
			return fullName.isBlank() ? user.email : fullName;
		});
		comboBox.setItems(userModels);
		Label emailLabel = new Label("placeholder");
		emailLabel.getStyle().set("visibility", "hidden");
		comboBox.addValueChangeListener(event -> Optional.ofNullable(event.getValue())
			.ifPresentOrElse(
				value -> {
					emailLabel.setText(value.email);
					emailLabel.getStyle().set("visibility", "visible");
				},
				() -> emailLabel.getStyle().set("visibility", "hidden"))
		);
		setSpacing(false);
		setMargin(false);
		getStyle().set("padding", "unset");
		comboBox.setClassName("furms-combo-box");
		add(comboBox, emailLabel);
	}
}
