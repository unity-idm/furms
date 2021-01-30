/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.domain.users;

import java.util.List;

public class UserRecord {
	public final UserStatus userStatus;
	public final List<Attribute> attributes;
	public final List<CommunityMembership> communities;
	
	public UserRecord(UserStatus userStatus, List<Attribute> attributes, List<CommunityMembership> communities) {
		this.userStatus = userStatus;
		this.attributes = List.copyOf(attributes);
		this.communities = List.copyOf(communities);
	}
}
