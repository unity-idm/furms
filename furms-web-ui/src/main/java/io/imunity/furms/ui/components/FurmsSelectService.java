/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.UI;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class FurmsSelectService {
	private final RoleTranslator roleTranslator;
	private final PersistentId currentId;

	FurmsSelectService(RoleTranslator roleTranslator, PersistentId currentId) {
		this.roleTranslator = roleTranslator;
		this.currentId = currentId;
	}

	List<FurmsSelectText> loadItems() {
		return roleTranslator.translateRolesToUserViewContexts(currentId).values().stream()
			.map(values -> values.stream()
					.sorted(Comparator.comparing(role -> role.name)))
			.flatMap(Stream::distinct)
			.map(FurmsSelectText::new)
			.collect(toList());
	}

	List<FurmsSelectText> reloadItems(){
		List<FurmsSelectText> items = loadItems();
		String id = loadSelectedItemId();
		
		for (int idx = 0; idx < items.size(); idx++) {
			FurmsSelectText furmsSelectText = items.get(idx);
			if (equalsIds(id, furmsSelectText.furmsViewUserContext.id)) {
				FurmsViewUserContext furmsViewUserContext = 
						new FurmsViewUserContext(furmsSelectText.furmsViewUserContext, false);
				FurmsSelectText selectText = new FurmsSelectText(furmsViewUserContext);
				items.set(idx, selectText);
			}
		}
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
