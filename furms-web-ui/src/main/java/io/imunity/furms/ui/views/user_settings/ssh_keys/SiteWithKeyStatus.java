/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;

import java.util.Objects;
import java.util.Optional;

import io.imunity.furms.domain.ssh_key_operation.SSHKeyOperation;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationStatus;

class SiteWithKeyStatus {
	public final String id;
	public final SSHKeyOperation keyOperation;
	public final SSHKeyOperationStatus keyOperationStatus;
	public final Optional<String> error;

	SiteWithKeyStatus(String id, SSHKeyOperation keyOperation, SSHKeyOperationStatus keyOperationStatus,
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
