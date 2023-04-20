/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("CommunityUpdateRequest")
public class AgentCommunityUpdateRequest implements Body
{
	public final String identifier;
	public final String name;
	public final String description;

	@JsonCreator
	public AgentCommunityUpdateRequest(String identifier, String name, String description)
	{
		this.identifier = identifier;
		this.name = name;
		this.description = description;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentCommunityUpdateRequest that = (AgentCommunityUpdateRequest) o;
		return Objects.equals(identifier, that.identifier) && Objects.equals(name, that.name) && Objects.equals(description, that.description);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(identifier, name, description);
	}

	@Override
	public String toString()
	{
		return "AgentCommunityUpdateRequest{" +
			"identifier='" + identifier + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			'}';
	}
}
