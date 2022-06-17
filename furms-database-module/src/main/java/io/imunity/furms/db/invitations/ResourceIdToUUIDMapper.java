/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.invitations;

import io.imunity.furms.domain.Id;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.UUID;

class ResourceIdToUUIDMapper {
	static UUID map(ResourceId resourceId) {
		switch (resourceId.type){
			case APP_LEVEL:
				return null;
			case SITE:
				return resourceId.asSiteId().id;
			case COMMUNITY:
				return resourceId.asCommunityId().id;
			case PROJECT:
				return resourceId.asProjectId().id;
			default:
				throw new IllegalArgumentException("This shouldn't happen. Resource type have to be set");
		}
	}

	static Id map(UUID id, ResourceType resourceType){
		switch (resourceType){
			case APP_LEVEL:
				return null;
			case SITE:
				return new SiteId(id);
			case COMMUNITY:
				return new CommunityId(id);
			case PROJECT:
				return new ProjectId(id);
			default:
				throw new IllegalArgumentException("This shouldn't happen. Resource type have to be set");
		}
	}
}
