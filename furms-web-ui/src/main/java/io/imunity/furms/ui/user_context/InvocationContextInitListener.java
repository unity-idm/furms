/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.user_context;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
class InvocationContextInitListener implements VaadinServiceInitListener
{
	@Override
	public void serviceInit(ServiceInitEvent serviceInitEvent)
	{
		serviceInitEvent.getSource().addUIInitListener(uiInitEvent ->
			InvocationContext.init(uiInitEvent.getUI())
		);
	}
}
