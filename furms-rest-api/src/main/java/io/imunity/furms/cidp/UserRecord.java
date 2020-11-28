/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.cidp;

import java.util.List;

public class UserRecord
{
	private UserStatus userStatus;
	private List<Attribute> attributes;
	private List<CommunityMembership> communities;

	public UserStatus getUserStatus()
	{
		return userStatus;
	}

	public List<Attribute> getAttributes()
	{
		return attributes;
	}

	public List<CommunityMembership> getCommunities()
	{
		return communities;
	}
}
