/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.cidp;

import io.imunity.furms.domain.users.UserSiteInstallation;

import java.util.Objects;
import java.util.Set;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

public class UserSiteInstallationJson {

	public final String siteId;
	public final String siteOauthClientId;
	public final Set<UserSiteInstallationProjectJson> projectSitesMemberships;
	public final PolicyAcceptanceJson sitePolicyAcceptance;
	public final Set<PolicyAcceptanceJson> servicesPolicyAcceptance;

	public UserSiteInstallationJson(UserSiteInstallation siteInstallation) {
		this.siteId = siteInstallation.siteId;
		this.siteOauthClientId = siteInstallation.siteOauthClientId;
		this.projectSitesMemberships = siteInstallation.projectSitesMemberships.stream()
				.map(UserSiteInstallationProjectJson::new)
				.collect(toSet());
		this.sitePolicyAcceptance = ofNullable(siteInstallation.sitePolicyAcceptance)
				.map(PolicyAcceptanceJson::new)
				.orElse(null);
		this.servicesPolicyAcceptance = ofNullable(siteInstallation.servicesPolicyAcceptance)
				.map(policyAcceptances -> policyAcceptances.stream()
						.map(PolicyAcceptanceJson::new)
						.collect(toSet()))
				.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserSiteInstallationJson that = (UserSiteInstallationJson) o;
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
		return "UserSiteInstallationJson{" +
				"siteId='" + siteId + '\'' +
				", siteOauthClientId='" + siteOauthClientId + '\'' +
				", projectSitesMemberships=" + projectSitesMemberships +
				", sitePolicyAcceptance=" + sitePolicyAcceptance +
				", servicesPolicyAcceptance=" + servicesPolicyAcceptance +
				'}';
	}
}
