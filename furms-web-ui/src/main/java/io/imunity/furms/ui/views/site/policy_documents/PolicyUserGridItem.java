/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.policy_documents;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.components.administrators.UserGridItem;

public class PolicyUserGridItem extends UserGridItem {
	private final int revision;
	private final boolean accepted;

	PolicyUserGridItem(FURMSUser user, int revision, int currentRevision){
		super(user);
		this.revision = revision;
		this.accepted = revision == currentRevision;
	}

	public int getRevision() {
		return revision;
	}

	boolean isAccepted() {
		return accepted;
	}
}
