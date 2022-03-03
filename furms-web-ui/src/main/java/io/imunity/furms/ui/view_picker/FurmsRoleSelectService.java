/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.view_picker;

import com.vaadin.flow.component.UI;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

class FurmsRoleSelectService {
	
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final RoleTranslator roleTranslator;
	private FurmsViewUserContext savedUserContext;
	
	FurmsRoleSelectService(RoleTranslator roleTranslator) {
		this.roleTranslator = roleTranslator;
	}

	List<FurmsRolePickerText> loadItems() {
		return roleTranslator.refreshAuthzRolesAndGetRolesToUserViewContexts().values().stream()
			.map(values -> values.stream().sorted())
			.flatMap(Stream::distinct)
			.map(FurmsRolePickerText::new)
			.collect(toList());
	}

	FurmsViewUserContext getCurrent() {
		return savedUserContext;
	}

	void manageSelectedItemRedirects(FurmsRolePickerText value){
		LOG.debug("Manage selected item redirects: {}", value);
		if(value == null)
			return;
		String id = loadSelectedItemId();
		value.furmsViewUserContext.setAsCurrent();
		savedUserContext = value.furmsViewUserContext;
		if (!value.furmsViewUserContext.id.equals(id)){
			LOG.debug("Redirecting to {}", value.furmsViewUserContext.route);
			UI.getCurrent().getInternals().setLastHandledNavigation(null);
			UI.getCurrent().navigate(value.furmsViewUserContext.route);
		}
	}

	
	void saveOrRestoreUserContext() {
		if (FurmsViewUserContext.getCurrent() == null) {
			if (savedUserContext == null) {
				LOG.warn("No saved user view context and nothing set in UI, troubles can be expected");
				return;
			}
			LOG.debug("Recreate furms user context from saved state {}", savedUserContext);
			savedUserContext.setAsCurrent();
		} else {
			savedUserContext = FurmsViewUserContext.getCurrent();
			LOG.debug("Updating saved UI state {}", savedUserContext);
		}
	}
	
	Optional<FurmsViewUserContext> loadSelectedItem(){
		return ofNullable(FurmsViewUserContext.getCurrent());
	}

	private String loadSelectedItemId() {
		return loadSelectedItem()
			.map(x -> x.id)
			.orElse(null);
	}
}
