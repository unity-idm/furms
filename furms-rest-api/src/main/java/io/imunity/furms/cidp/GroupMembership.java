/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.cidp;

import java.util.List;

public class GroupMembership
{
	private String name;
	private List<Attribute> attributes;

	public String getName()
	{
		return name;
	}
	
	public List<Attribute> getAttributes()
	{
		return attributes;
	}
}
