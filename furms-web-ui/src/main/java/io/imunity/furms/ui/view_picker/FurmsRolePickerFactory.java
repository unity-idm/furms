/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.view_picker;

import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.user_context.RoleTranslator;
import org.springframework.stereotype.Component;

@Component
public class FurmsRolePickerFactory {
	private final RoleTranslator roleTranslator;
	private final VaadinBroadcaster vaadinBroadcaster;

	FurmsRolePickerFactory(RoleTranslator roleTranslator, VaadinBroadcaster vaadinBroadcaster) {
		this.roleTranslator = roleTranslator;
		this.vaadinBroadcaster = vaadinBroadcaster;
	}

	public FurmsRolePicker create(){
		return new FurmsRolePicker(roleTranslator, vaadinBroadcaster);
	}
}
