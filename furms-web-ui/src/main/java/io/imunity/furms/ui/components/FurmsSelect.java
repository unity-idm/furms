/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.events.CRUD;
import io.imunity.furms.api.events.FurmsEvent;
import io.imunity.furms.api.events.UserEvent;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@VaadinSessionScope
@CssImport("./styles/components/furms-select.css")
public class FurmsSelect extends Select<FurmsSelectText> {
	private final FurmsSelectService furmsSelectService;
	private final AuthzService authzService;

	FurmsSelect(RoleTranslator roleTranslator, AuthzService authzService) {
		this.authzService = authzService;
		furmsSelectService = new FurmsSelectService(roleTranslator);
		List<FurmsSelectText> items = furmsSelectService.loadItems();

		setClassName("furms-select");
		setItems(items);
		setTextRenderer(Text::getText);
		//addSeparators(data); TODO FIX separators are disabled now

		furmsSelectService.loadSelectedItem()
			.ifPresent(userContext -> setValue(new FurmsSelectText(userContext)));

		addValueChangeListener(event -> furmsSelectService.manageSelectedItemRedirects(event.getValue()));
	}

	@EventListener
	public void handleUserEvents(FurmsEvent<UserEvent> event) {
		if(event.crud == CRUD.CREATE || event.crud == CRUD.DELETE)
			if(authzService.getCurrentUserId().equals(event.entity.id))
				reloadComponent();
	}

	@EventListener
	public void handleProjectEvents(FurmsEvent<Project> event) {
		if(event.crud == CRUD.CREATE || event.crud == CRUD.DELETE || event.crud == CRUD.UPDATE)
			reloadComponent();
	}

	@EventListener
	public void handleCommunityEvents(FurmsEvent<Community> event) {
		if(event.crud == CRUD.CREATE || event.crud == CRUD.DELETE || event.crud == CRUD.UPDATE)
			reloadComponent();
	}

	@EventListener
	public void handleSiteEvents(FurmsEvent<Site> event) {
		if(event.crud == CRUD.CREATE || event.crud == CRUD.DELETE || event.crud == CRUD.UPDATE)
			reloadComponent();
	}

	void reloadComponent(){
		String currentSelectedContextId = getValue().furmsViewUserContext.id;
		List<FurmsSelectText> items = furmsSelectService.reloadItems();
		setItems(items);
		items.stream()
			.filter(selectText -> selectText.furmsViewUserContext.id.equals(currentSelectedContextId))
			.findFirst()
			.ifPresent(userContext -> setValue(userContext));
	}


	@SuppressWarnings("unused")
	private void addSeparators(Map<ViewMode, List<FurmsViewUserContext>> data) {
		FurmsSelectText component = null;
		for (Map.Entry<ViewMode, List<FurmsViewUserContext>> entry : data.entrySet()) {
			if(entry.getKey() == ViewMode.USER){
				continue;
			}
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
