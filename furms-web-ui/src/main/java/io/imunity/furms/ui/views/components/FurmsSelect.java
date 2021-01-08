/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.components;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import io.imunity.furms.domain.authz.UserScopeContent;
import io.imunity.furms.domain.authz.roles.RoleLevel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

public class FurmsSelect extends Select<FurmsSelectText> {
	public FurmsSelect(Map<RoleLevel, List<UserScopeContent>> data) {
		setItems(data.values().stream().flatMap(Collection::stream).map(FurmsSelectText::new));
		addSeparators(data);
		setTextRenderer(Text::getText);
		addValueChangeListener(x -> {
			UI.getCurrent().getSession().setAttribute(UserScopeContent.class, x.getValue().userScopeContent);
			UI.getCurrent().navigate(x.getValue().userScopeContent.redirectURI);
		});
		ofNullable(UI.getCurrent().getSession().getAttribute(UserScopeContent.class))
			.ifPresent(x -> setValue(new FurmsSelectText(x)));
	}

	private void addSeparators(Map<RoleLevel, List<UserScopeContent>> data) {
		FurmsSelectText component = null;
		for (Map.Entry<RoleLevel, List<UserScopeContent>> entry : data.entrySet()) {
			if(component != null){
				Span text = new Span(entry.getKey().name());
				text.addClassName("select-span-separator");
				addComponents(component, new Hr());
				addComponents(component, text);
				addComponents(component, new Hr());
			}
			if(entry.getValue().size() > 0){
				component = new FurmsSelectText(entry.getValue().get(entry.getValue().size() - 1));
			}
		}
	}
}
