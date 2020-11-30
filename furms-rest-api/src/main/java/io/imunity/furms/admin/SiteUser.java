/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.admin;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

class SiteUser {
	final User user;
	final String uid;
	final List<String> sshKeys;
	final Set<String> projectIds; 
	
	SiteUser(User user, String uid, List<String> sshKeys, Set<String> projectIds) {
		this.user = user;
		this.uid = uid;
		this.projectIds = ImmutableSet.copyOf(projectIds);
		this.sshKeys = ImmutableList.copyOf(sshKeys);
	}
}
