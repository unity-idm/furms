/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.cidp;

import java.util.List;

public class CommunityMembership
{
	private String name;
	private List<GroupMembership> groups;
	private List<ProjectMembership> projects;
	private List<Attribute> attributes;

	public String getName()
	{
		return name;
	}
	public List<GroupMembership> getGroups()
	{
		return groups;
	}
	public List<ProjectMembership> getProjects()
	{
		return projects;
	}
	public List<Attribute> getAttributes()
	{
		return attributes;
	}
}
