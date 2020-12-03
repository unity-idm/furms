/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.communites;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.fenix_admin.menu.FenixAdminMenu;

@Route(value = "fenix/admin/community", layout = FenixAdminMenu.class)
@PageTitle(key = "view.community.page.title")
public class CommunityView extends FurmsViewComponent {
	private final CommunityService communityService;

	CommunityView(CommunityService communityService) {
		this.communityService = communityService;
	}

	@Override
	public void setParameter(BeforeEvent event, String communityId) {
		Community community = communityService.findById(communityId).orElseThrow(IllegalStateException::new);
		getContent().add(new Label(community.toString()));
	}

}
