/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui;

import org.springframework.stereotype.Component;

import io.imunity.furms.ui.components.FurmsSelect;
import io.imunity.furms.ui.user_context.RoleTranslator;

@Component
public class FurmsSelectFactory {
	private final RoleTranslator roleTranslator;
	private final VaadinBroadcaster vaadinBroadcaster;

	public FurmsSelectFactory(RoleTranslator roleTranslator, VaadinBroadcaster vaadinBroadcaster) {
		this.roleTranslator = roleTranslator;
		this.vaadinBroadcaster = vaadinBroadcaster;
	}

	public FurmsSelect create(){
		return new FurmsSelect(roleTranslator, vaadinBroadcaster);
	}
}
