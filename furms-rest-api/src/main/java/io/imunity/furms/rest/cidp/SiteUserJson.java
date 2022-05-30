/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.cidp;

import io.imunity.furms.domain.sites.SiteUser;

import java.util.Objects;
import java.util.Set;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

public class SiteUserJson {

	public final String siteId;
	public final String siteOauthClientId;
	public final Set<ProjectMembershipOnSiteJson> projectMemberships;
	public final PolicyAcceptanceJson sitePolicyAcceptance;
	public final Set<PolicyAcceptanceJson> servicesPolicyAcceptance;
	public final Set<String> sshKeys;

	public SiteUserJson(SiteUser siteUser) {
		this.siteId = siteUser.siteId.id.toString();
		this.siteOauthClientId = siteUser.siteOauthClientId;
		this.projectMemberships = siteUser.projectMemberships.stream()
				.map(ProjectMembershipOnSiteJson::new)
				.collect(toSet());
		this.sitePolicyAcceptance = ofNullable(siteUser.sitePolicyAcceptance)
				.map(PolicyAcceptanceJson::new)
				.orElse(null);
		this.servicesPolicyAcceptance = ofNullable(siteUser.servicesPolicyAcceptance)
				.map(policyAcceptances -> policyAcceptances.stream()
						.map(PolicyAcceptanceJson::new)
						.collect(toSet()))
				.orElse(null);
		this.sshKeys = ofNullable(siteUser.siteSSHKeys)
				.map(keys -> keys.sshKeys)
				.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteUserJson that = (SiteUserJson) o;
		return Objects.equals(siteId, that.siteId)
				&& Objects.equals(siteOauthClientId, that.siteOauthClientId)
				&& Objects.equals(projectMemberships, that.projectMemberships)
				&& Objects.equals(sitePolicyAcceptance, that.sitePolicyAcceptance)
				&& Objects.equals(servicesPolicyAcceptance, that.servicesPolicyAcceptance)
				&& Objects.equals(sshKeys, that.sshKeys);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, siteOauthClientId, projectMemberships, sitePolicyAcceptance,
				servicesPolicyAcceptance, sshKeys);
	}

	@Override
	public String toString() {
		return "SiteUserJson{" +
				"siteId='" + siteId + '\'' +
				", siteOauthClientId='" + siteOauthClientId + '\'' +
				", projectMemberships=" + projectMemberships +
				", sitePolicyAcceptance=" + sitePolicyAcceptance +
				", servicesPolicyAcceptance=" + servicesPolicyAcceptance +
				", sshKeys=" + sshKeys +
				'}';
	}
}
