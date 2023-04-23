/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.site_agent.AvailabilityStatus;

import java.util.Objects;

class SiteAvailability
{
	final AvailabilityStatus status;

	SiteAvailability(AvailabilityStatus status)
	{
		this.status = status;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteAvailability that = (SiteAvailability) o;
		return Objects.equals(status, that.status);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(status);
	}

	@Override
	public String toString()
	{
		return "SiteAvailability{" +
			"status=" + status +
			'}';
	}
}
