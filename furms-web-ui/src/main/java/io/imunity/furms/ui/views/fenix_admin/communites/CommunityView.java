/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.communites;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.components.Parameter;
import io.imunity.furms.ui.views.fenix_admin.menu.FenixAdminMenu;

import java.util.Optional;

@Route(value = "fenix/admin/community", layout = FenixAdminMenu.class)
@PageTitle(key = "view.community.page.title")
public class CommunityView extends FurmsViewComponent {
	private final CommunityService communityService;

	private Parameter parameter;

	CommunityView(CommunityService communityService) {
		this.communityService = communityService;
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String communityId) {
		Community community = communityService.findById(communityId).orElseThrow(IllegalStateException::new);
		parameter = new Parameter(community.getId(), community.getName());
		getContent().removeAll();
		getContent().add(new Label(community.getId()));
	}

	@Override
	public Optional<Parameter> getParameter() {
		return Optional.ofNullable(parameter);
	}
}
