/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import io.imunity.furms.domain.policy_documents.PolicyAgreementExtended;

import java.util.Objects;
import java.util.Set;

public class UserSiteInstallation {

	public final String siteId;
	public final String siteOauthClientId;
	public final Set<UserSiteInstallationProject> projectSitesMemberships;
	public final PolicyAgreementExtended sitePolicyAcceptance;
	public final Set<PolicyAgreementExtended> servicesPolicyAcceptance;

	public UserSiteInstallation(String siteId,
	                            String siteOauthClientId,
	                            Set<UserSiteInstallationProject> projectSitesMemberships,
	                            PolicyAgreementExtended sitePolicyAcceptance,
	                            Set<PolicyAgreementExtended> servicesPolicyAcceptance) {
		this.siteId = siteId;
		this.siteOauthClientId = siteOauthClientId;
		this.projectSitesMemberships = projectSitesMemberships;
		this.sitePolicyAcceptance = sitePolicyAcceptance;
		this.servicesPolicyAcceptance = servicesPolicyAcceptance;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserSiteInstallation that = (UserSiteInstallation) o;
		return Objects.equals(siteId, that.siteId)
				&& Objects.equals(siteOauthClientId, that.siteOauthClientId)
				&& Objects.equals(projectSitesMemberships, that.projectSitesMemberships)
				&& Objects.equals(sitePolicyAcceptance, that.sitePolicyAcceptance)
				&& Objects.equals(servicesPolicyAcceptance, that.servicesPolicyAcceptance);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, siteOauthClientId, projectSitesMemberships, sitePolicyAcceptance, servicesPolicyAcceptance);
	}

	@Override
	public String toString() {
		return "UserSiteInstallation{" +
				"siteId='" + siteId + '\'' +
				", siteOauthClientId='" + siteOauthClientId + '\'' +
				", projectSitesMemberships=" + projectSitesMemberships +
				", sitePolicyAcceptance=" + sitePolicyAcceptance +
				", servicesPolicyAcceptance=" + servicesPolicyAcceptance +
				'}';
	}

	public static UserSiteInstallationBuilder builder() {
		return new UserSiteInstallationBuilder();
	}


	public static final class UserSiteInstallationBuilder {
		public String siteId;
		public String siteOauthClientId;
		public Set<UserSiteInstallationProject> projectSitesMemberships;
		public PolicyAgreementExtended sitePolicyAcceptance;
		public Set<PolicyAgreementExtended> servicesPolicyAcceptance;

		private UserSiteInstallationBuilder() {
		}

		public UserSiteInstallationBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public UserSiteInstallationBuilder siteOauthClientId(String siteOauthClientId) {
			this.siteOauthClientId = siteOauthClientId;
			return this;
		}

		public UserSiteInstallationBuilder projectSitesMemberships(Set<UserSiteInstallationProject> projectSitesMemberships) {
			this.projectSitesMemberships = projectSitesMemberships;
			return this;
		}

		public UserSiteInstallationBuilder sitePolicyAcceptance(PolicyAgreementExtended sitePolicyAcceptance) {
			this.sitePolicyAcceptance = sitePolicyAcceptance;
			return this;
		}

		public UserSiteInstallationBuilder servicesPolicyAcceptance(Set<PolicyAgreementExtended> servicesPolicyAcceptance) {
			this.servicesPolicyAcceptance = servicesPolicyAcceptance;
			return this;
		}

		public UserSiteInstallation build() {
			return new UserSiteInstallation(siteId, siteOauthClientId, projectSitesMemberships, sitePolicyAcceptance, servicesPolicyAcceptance);
		}
	}
}
