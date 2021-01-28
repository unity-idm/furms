/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.UI;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class FurmsSelectService {
	private final RoleTranslator roleTranslator;

	FurmsSelectService(RoleTranslator roleTranslator) {
		this.roleTranslator = roleTranslator;
	}

	List<FurmsSelectText> loadItems() {
		return roleTranslator.translateRolesToUserViewContexts().values().stream()
			.flatMap(Collection::stream)
			.map(FurmsSelectText::new)
			.collect(toList());
	}

	List<FurmsSelectText> reloadItems(){
		List<FurmsSelectText> items = loadItems();
		String id = loadSelectedItemId();
		items.stream()
			.filter(selectText -> equalsIds(id, selectText.furmsViewUserContext.id))
			.findAny()
			.ifPresent(furmsSelectText -> {
				FurmsViewUserContext furmsViewUserContext =
					new FurmsViewUserContext(furmsSelectText.furmsViewUserContext, false);
				FurmsSelectText selectText = new FurmsSelectText(furmsViewUserContext);
				setSelectedItem(furmsViewUserContext);
				items.remove(furmsSelectText);
				items.add(selectText);
			});
		return items;
	}

	private boolean equalsIds(String lastId, String currentId) {
		if(lastId == null && currentId == null)
			return true;
		return Optional.ofNullable(currentId)
			.map(id -> id.equals(lastId))
			.orElse(false);
	}

	void manageSelectedItemRedirects(FurmsSelectText value){
		ofNullable(value)
			.ifPresent(furmsSelectText -> setSelectedItem(furmsSelectText.furmsViewUserContext));
		if(value != null && value.furmsViewUserContext.redirectable){
			UI.getCurrent().navigate(value.furmsViewUserContext.route);
		}
	}

	Optional<FurmsViewUserContext> loadSelectedItem(){
		return ofNullable(UI.getCurrent().getSession().getAttribute(FurmsViewUserContext.class));
	}

	private void setSelectedItem(FurmsViewUserContext furmsViewUserContext) {
		UI.getCurrent().getSession().setAttribute(FurmsViewUserContext.class, furmsViewUserContext);
	}

	private String loadSelectedItemId() {
		return loadSelectedItem()
			.map(x -> x.id)
			.orElse(null);
	}
}
