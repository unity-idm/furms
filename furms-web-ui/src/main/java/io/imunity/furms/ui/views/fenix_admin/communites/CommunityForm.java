/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.communites;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import io.imunity.furms.domain.communities.Community;

class CommunityForm extends FormLayout {
	Binder<Community> binder = new BeanValidationBinder<>(Community.class);
	TextField id = new TextField("ID");
	TextField name = new TextField("Community Name");
	TextField description = new TextField("Description");

	Button save = new Button("Save");
	Button close = new Button("Cancel");

	public CommunityForm() {
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		save.addClickShortcut(Key.ENTER);
		close.addClickShortcut(Key.ESCAPE);
		close.addClickListener(x -> closeEditor());


		//binder.bindInstanceFields(this);
		//binder.setValidatorsDisabled(true);
//		binder.forField(name)
//			.bind(Community::getName, Community:)
//		binder.addStatusChangeListener(env -> save.setEnabled(binder.isValid()));

		add(name, description, new HorizontalLayout(save, close));
	}

	public void closeEditor(){
		setContent(null);
		setVisible(false);
	}

	public void openEditor(Community community){
		setContent(community);
		setVisible(true);
	}

	public void setContent(Community community){
		binder.setBean(community);
	}
}
