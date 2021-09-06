/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.landing;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.invitations.InvitationService;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;

import static io.imunity.furms.domain.constant.RoutesConst.REGISTRATION_LANDING_PAGE_URL;
import static java.util.Collections.emptyList;

@Route(REGISTRATION_LANDING_PAGE_URL)
@PageTitle(key = "view.landing.title")
class RegistrationLandingPageView extends FurmsViewComponent {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final InvitationService invitationService;

	RegistrationLandingPageView(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		Iterator<String> parameters = event.getLocation()
			.getQueryParameters()
			.getParameters()
			.getOrDefault("request_id", emptyList())
			.iterator();
		if(parameters.hasNext())
			invitationService.deleteBy(parameters.next());
		else
			LOG.warn("No unity code to removing invitation");

		UI.getCurrent().navigate(LandingPageView.class);
	}

}
