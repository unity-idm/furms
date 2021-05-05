/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.users;

import java.util.Objects;
import java.util.Set;

public class UserSSHKey {
	public final String id;
	public final String value;
	public final Set<String> sites;

	public UserSSHKey(String id, String value, Set<String> sites) {
		this.id = id;
		this.value = value;
		this.sites = Set.copyOf(sites);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sites, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserSSHKey other = (UserSSHKey) obj;
		return Objects.equals(sites, other.sites) && Objects.equals(value, other.value)  && Objects.equals(id, other.id);
	}
}
