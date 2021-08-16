/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui;

import io.imunity.furms.ui.components.FurmsSelect;
import io.imunity.furms.ui.user_context.RoleTranslator;
import org.springframework.stereotype.Component;

@Component
public class FurmsSelectFactory {
	private final RoleTranslator roleTranslator;
	private final VaadinBroadcaster vaadinBroadcaster;

	FurmsSelectFactory(RoleTranslator roleTranslator, VaadinBroadcaster vaadinBroadcaster) {
		this.roleTranslator = roleTranslator;
		this.vaadinBroadcaster = vaadinBroadcaster;
	}

	public FurmsSelect create(){
		return new FurmsSelect(roleTranslator, vaadinBroadcaster);
	}
}
