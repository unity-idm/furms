/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.cidp;

import static java.util.stream.Collectors.toList;

import java.util.List;

import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserStatus;

public class UserRecordJson {
	public final UserStatus userStatus;
	public final List<AttributeJson> attributes;
	public final List<CommunityMembershipJson> communities;
	public final List<SiteSSHKeysJson> sshKeys;
	public final List<UserSiteInstallationJson> siteInstallations;
	
	UserRecordJson(UserStatus userStatus, List<AttributeJson> attributes, List<CommunityMembershipJson> communities,
	               List<SiteSSHKeysJson> sshKeys, List<UserSiteInstallationJson> siteInstallations) {
		this.userStatus = userStatus;
		this.attributes = List.copyOf(attributes);
		this.communities = List.copyOf(communities);
		this.sshKeys = List.copyOf(sshKeys);
		this.siteInstallations = List.copyOf(siteInstallations);
	}
	
	UserRecordJson(UserRecord record) {
		this(record.userStatus,
				record.attributes.stream().map(AttributeJson::new).collect(toList()), 
				record.communities.stream().map(CommunityMembershipJson::new).collect(toList()),
				record.sshKeys.stream().map(SiteSSHKeysJson::new).collect(toList()),
				record.siteInstallations.stream().map(UserSiteInstallationJson::new).collect(toList()));
	}
}
