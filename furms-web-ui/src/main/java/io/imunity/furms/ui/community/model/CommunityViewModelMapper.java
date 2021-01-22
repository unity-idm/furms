/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.community.model;

import io.imunity.furms.domain.communities.Community;

public class CommunityViewModelMapper {
	public static CommunityViewModel map(Community community){
		return CommunityViewModel.builder()
			.id(community.getId())
			.name(community.getName())
			.description(community.getDescription())
			.logo(community.getLogo())
			.build();
	}

	public static Community map(CommunityViewModel community){
		return Community.builder()
			.id(community.getId())
			.name(community.getName())
			.description(community.getDescription())
			.logo(community.getLogoImage())
			.build();
	}
}
