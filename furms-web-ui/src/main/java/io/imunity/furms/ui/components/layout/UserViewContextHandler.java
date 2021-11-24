/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.layout;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@Component
public class UserViewContextHandler {

	private final static String PARAM_RESOURCE_ID = "resourceId";

	private final RoleTranslator roleTranslator;

	UserViewContextHandler(RoleTranslator roleTranslator) {
		this.roleTranslator = roleTranslator;
	}

	void setUserViewContext(BeforeEnterEvent beforeEnterEvent, ViewMode viewMode) {
		beforeEnterEvent.getLocation()
				.getSubLocation()
				.map(Location::getQueryParameters)
				.flatMap(queryParameters -> ofNullable(queryParameters.getParameters().get(PARAM_RESOURCE_ID)))
				.filter(resourceId -> !resourceId.isEmpty())
				.map(resourceId -> resourceId.iterator().next())
				.ifPresentOrElse(
						resourceId -> setUserViewContext(resourceId, viewMode),
						() -> setUserViewContext(viewMode));
	}

	private void setUserViewContext(String resourceId, ViewMode viewMode) {
		findViewUserContextsByViewMode(viewMode)
				.filter(viewUserContext -> viewUserContext.id.equals(resourceId))
				.findAny()
				.ifPresent(FurmsViewUserContext::setAsCurrent);
	}

	private void setUserViewContext(ViewMode viewMode) {
		findViewUserContextsByViewMode(viewMode)
				.findAny()
				.ifPresent(FurmsViewUserContext::setAsCurrent);
	}

	private Stream<FurmsViewUserContext> findViewUserContextsByViewMode(ViewMode viewMode) {
		return roleTranslator.refreshAuthzRolesAndGetRolesToUserViewContexts()
				.getOrDefault(viewMode, emptyList())
				.stream();
	}

}
