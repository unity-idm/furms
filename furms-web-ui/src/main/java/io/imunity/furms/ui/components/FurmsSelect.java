/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.shared.Registration;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.VaadinBroadcaster;

import java.util.List;
import java.util.Map;

@CssImport("./styles/components/furms-select.css")
public class FurmsSelect extends Select<FurmsSelectText> {
	private final FurmsSelectService furmsSelectService;
	private final VaadinBroadcaster vaadinBroadcaster;
	private Registration broadcasterRegistration;

	public FurmsSelect(RoleTranslator roleTranslator, VaadinBroadcaster vaadinBroadcaster) {
		furmsSelectService = new FurmsSelectService(roleTranslator);
		this.vaadinBroadcaster = vaadinBroadcaster;
		List<FurmsSelectText> items = furmsSelectService.loadItems();

		setClassName("furms-select");
		setItems(items);
		setTextRenderer(Text::getText);
		//addSeparators(data); TODO FIX separators are disabled now

		furmsSelectService.loadSelectedItem()
			.ifPresent(userContext -> setValue(new FurmsSelectText(userContext)));

		addValueChangeListener(event -> furmsSelectService.manageSelectedItemRedirects(event.getValue()));
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

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		UI ui = attachEvent.getUI();
		broadcasterRegistration = vaadinBroadcaster.register(
			event -> ui.access(this::reloadComponent)
		);
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		broadcasterRegistration.remove();
		broadcasterRegistration = null;
	}
}
