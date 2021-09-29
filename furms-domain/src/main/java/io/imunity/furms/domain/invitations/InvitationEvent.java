/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.invitations;

import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.users.FenixUserId;

public interface InvitationEvent extends FurmsEvent {
	FenixUserId getId();
	String getEmail();
}
