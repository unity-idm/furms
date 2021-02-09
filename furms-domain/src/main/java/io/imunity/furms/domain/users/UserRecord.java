/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.domain.users;

import java.util.Collection;
import java.util.Set;

public class UserRecord {
	public final UserStatus userStatus;
	public final Set<Attribute> attributes;
	public final Set<CommunityMembership> communities;
	
	public UserRecord(UserStatus userStatus, Collection<Attribute> attributes, Collection<CommunityMembership> communities) {
		this.userStatus = userStatus;
		this.attributes = Set.copyOf(attributes);
		this.communities = Set.copyOf(communities);
	}
}
