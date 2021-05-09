/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import java.util.Objects;

public class SSHKeyOperationResult {
	public final SSHKeyOperationStatus status;
	public final SSHKeyOperationError error;

	public SSHKeyOperationResult(SSHKeyOperationStatus status, SSHKeyOperationError error) {

		this.status = status;
		this.error = error;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SSHKeyOperationResult that = (SSHKeyOperationResult) o;
		return status == that.status && Objects.equals(error, that.error);
	}

	@Override
	public int hashCode() {
		return Objects.hash(status, error);
	}

	@Override
	public String toString() {
		return "SSHKeyOperationResult{" +
				"status=" + status + ", error=" + error + '}';
	}
}
