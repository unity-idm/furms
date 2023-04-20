/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("CommunityRemovalRequest")
public class AgentCommunityRemovalRequest implements Body
{
	public final String identifier;

	@JsonCreator
	public AgentCommunityRemovalRequest(String identifier)
	{
		this.identifier = identifier;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentCommunityRemovalRequest that = (AgentCommunityRemovalRequest) o;
		return Objects.equals(identifier, that.identifier);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(identifier);
	}

	@Override
	public String toString()
	{
		return "AgentCommunityRemovalRequest{" +
			"identifier='" + identifier + '\'' +
			'}';
	}
}
