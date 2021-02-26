/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.landing;

import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.FurmsSelectFactory;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;

import static io.imunity.furms.domain.constant.RoutesConst.ROLE_CHOOSER_URL;

@Route(ROLE_CHOOSER_URL)
@PageTitle(key = "view.landing.title")
public class RoleChooserView extends FurmsViewComponent {
	RoleChooserView(FurmsSelectFactory furmsSelectFactory) {
		RoleChooserLayout roleChooserLayout = new RoleChooserLayout(furmsSelectFactory.create());
		getContent().add(roleChooserLayout);
	}
}
