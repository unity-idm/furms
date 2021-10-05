/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import io.imunity.furms.domain.policy_documents.PolicyAcceptanceAtSite;
import io.imunity.furms.domain.projects.ProjectMembershipOnSite;
import io.imunity.furms.domain.users.SiteSSHKeys;

import java.util.Objects;
import java.util.Set;

public class SiteUser {

	public final String siteId;
	public final String siteOauthClientId;
	public final Set<ProjectMembershipOnSite> projectMemberships;
	public final PolicyAcceptanceAtSite sitePolicyAcceptance;
	public final Set<PolicyAcceptanceAtSite> servicesPolicyAcceptance;
	public final SiteSSHKeys siteSSHKeys;

	public SiteUser(String siteId,
	                String siteOauthClientId,
	                Set<ProjectMembershipOnSite> projectMemberships,
	                PolicyAcceptanceAtSite sitePolicyAcceptance,
	                Set<PolicyAcceptanceAtSite> servicesPolicyAcceptance,
	                SiteSSHKeys siteSSHKeys) {
		this.siteId = siteId;
		this.siteOauthClientId = siteOauthClientId;
		this.projectMemberships = projectMemberships;
		this.sitePolicyAcceptance = sitePolicyAcceptance;
		this.servicesPolicyAcceptance = servicesPolicyAcceptance;
		this.siteSSHKeys = siteSSHKeys;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteUser siteUser = (SiteUser) o;
		return Objects.equals(siteId, siteUser.siteId)
				&& Objects.equals(siteOauthClientId, siteUser.siteOauthClientId)
				&& Objects.equals(projectMemberships, siteUser.projectMemberships)
				&& Objects.equals(sitePolicyAcceptance, siteUser.sitePolicyAcceptance)
				&& Objects.equals(servicesPolicyAcceptance, siteUser.servicesPolicyAcceptance)
				&& Objects.equals(siteSSHKeys, siteUser.siteSSHKeys);
	}

	@Override
	public String toString() {
		return "SiteUser{" +
				"siteId='" + siteId + '\'' +
				", siteOauthClientId='" + siteOauthClientId + '\'' +
				", projectMemberships=" + projectMemberships +
				", sitePolicyAcceptance=" + sitePolicyAcceptance +
				", servicesPolicyAcceptance=" + servicesPolicyAcceptance +
				", siteSSHKeys=" + siteSSHKeys +
				'}';
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, siteOauthClientId, projectMemberships, sitePolicyAcceptance, servicesPolicyAcceptance, siteSSHKeys);
	}

	public static SiteUserBuilder builder() {
		return new SiteUserBuilder();
	}


	public static final class SiteUserBuilder {
		private String siteId;
		private String siteOauthClientId;
		private Set<ProjectMembershipOnSite> projectMemberships;
		private PolicyAcceptanceAtSite sitePolicyAcceptance;
		private Set<PolicyAcceptanceAtSite> servicesPolicyAcceptance;
		private SiteSSHKeys siteSSHKeys;

		private SiteUserBuilder() {
		}

		public SiteUserBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public SiteUserBuilder siteOauthClientId(String siteOauthClientId) {
			this.siteOauthClientId = siteOauthClientId;
			return this;
		}

		public SiteUserBuilder projectSitesMemberships(Set<ProjectMembershipOnSite> projectMemberships) {
			this.projectMemberships = projectMemberships;
			return this;
		}

		public SiteUserBuilder sitePolicyAcceptance(PolicyAcceptanceAtSite sitePolicyAcceptance) {
			this.sitePolicyAcceptance = sitePolicyAcceptance;
			return this;
		}

		public SiteUserBuilder servicesPolicyAcceptance(Set<PolicyAcceptanceAtSite> servicesPolicyAcceptance) {
			this.servicesPolicyAcceptance = servicesPolicyAcceptance;
			return this;
		}

		public SiteUserBuilder sshKeys(SiteSSHKeys siteSSHKeys) {
			this.siteSSHKeys = siteSSHKeys;
			return this;
		}

		public SiteUser build() {
			return new SiteUser(siteId, siteOauthClientId, projectMemberships, sitePolicyAcceptance,
					servicesPolicyAcceptance, siteSSHKeys);
		}
	}
}
