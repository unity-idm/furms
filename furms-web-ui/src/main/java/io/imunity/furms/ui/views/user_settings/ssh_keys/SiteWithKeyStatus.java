/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;

import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperation;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;

import java.util.Objects;
import java.util.Optional;

class SiteWithKeyStatus {
	public final SiteId id;
	public final SSHKeyOperation keyOperation;
	public final SSHKeyOperationStatus keyOperationStatus;
	public final Optional<String> error;

	SiteWithKeyStatus(SiteId id, SSHKeyOperation keyOperation, SSHKeyOperationStatus keyOperationStatus,
	                  String error) {
		this.id = id;
		this.keyOperation = keyOperation;
		this.keyOperationStatus = keyOperationStatus;
		this.error = Optional.ofNullable(error);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, keyOperation, keyOperationStatus, error);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SiteWithKeyStatus other = (SiteWithKeyStatus) obj;
		return Objects.equals(id, other.id) && keyOperation == other.keyOperation
				&& keyOperationStatus == other.keyOperationStatus && Objects.equals(error, other.error);
	}

}
