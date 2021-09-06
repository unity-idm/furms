/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.invitations;

import java.util.Objects;

public class InvitationCode {
	public final String code;

	public InvitationCode(String code) {
		this.code = code;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InvitationCode that = (InvitationCode) o;
		return Objects.equals(code, that.code);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code);
	}

	@Override
	public String toString() {
		return "InvitationCode{" +
			"code='" + code + '\'' +
			'}';
	}
}
