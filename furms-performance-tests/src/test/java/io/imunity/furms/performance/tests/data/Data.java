/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.performance.tests.data;

import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;
import java.util.Set;

class Data {

	static class Results {
		public final Set<Site> sites;
		public final Set<Community> communities;
		public final User fenixAdmin;
		public final User communitiesAdmin;
		public final User projectsAdmin;

		public Results(Set<Site> sites, Set<Community> communities, User fenixAdmin, User communitiesAdmin, User projectsAdmin) {
			this.sites = sites;
			this.communities = communities;
			this.fenixAdmin = fenixAdmin;
			this.communitiesAdmin = communitiesAdmin;
			this.projectsAdmin = projectsAdmin;
		}
	}

	static class Site {
		public final SiteId siteId;
		public final PolicyId policyId;
		public final String resourceTypeId;

		public Site(SiteId siteId, PolicyId policyId, String resourceTypeId) {
			this.siteId = siteId;
			this.policyId = policyId;
			this.resourceTypeId = resourceTypeId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Site site = (Site) o;
			return Objects.equals(siteId, site.siteId) && Objects.equals(policyId, site.policyId) && Objects.equals(resourceTypeId, site.resourceTypeId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(siteId, policyId, resourceTypeId);
		}
	}

	static class Community {
		public final String communityId;
		public final Set<String> projectIds;

		public Community(String communityId, Set<String> projectIds) {
			this.communityId = communityId;
			this.projectIds = projectIds;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Community community = (Community) o;
			return Objects.equals(communityId, community.communityId) && Objects.equals(projectIds, community.projectIds);
		}

		@Override
		public int hashCode() {
			return Objects.hash(communityId, projectIds);
		}
	}

	static class Project {
		public final String projectId;
		public final String communityId;

		public Project(String projectId, String communityId) {
			this.projectId = projectId;
			this.communityId = communityId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Project project = (Project) o;
			return Objects.equals(projectId, project.projectId) && Objects.equals(communityId, project.communityId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(projectId, communityId);
		}
	}

	static class User {
		public final String persistentId;
		public final long entityId;
		public final String fenixUserId;
		public final String apiKey;

		public User(String persistentId, long entityId, String fenixUserId) {
			this.persistentId = persistentId;
			this.entityId = entityId;
			this.fenixUserId = fenixUserId;
			this.apiKey = null;
		}

		public User(String persistentId, long entityId, String fenixUserId, String apiKey) {
			this.persistentId = persistentId;
			this.entityId = entityId;
			this.fenixUserId = fenixUserId;
			this.apiKey = apiKey;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			User user = (User) o;
			return entityId == user.entityId && Objects.equals(persistentId, user.persistentId) && Objects.equals(fenixUserId, user.fenixUserId) && Objects.equals(apiKey, user.apiKey);
		}

		@Override
		public int hashCode() {
			return Objects.hash(persistentId, entityId, fenixUserId, apiKey);
		}
	}

}
