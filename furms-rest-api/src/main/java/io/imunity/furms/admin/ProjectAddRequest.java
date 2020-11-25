/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class ProjectAddRequest
{
	final String communityId;
	final String name;
	final String description;

	ProjectAddRequest(String communityId, String name, String description)
	{
		this.communityId = communityId;
		this.name = name;
		this.description = description;
	}

}
