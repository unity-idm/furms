/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.util;

import io.imunity.furms.core.config.security.user.FurmsOAuth2User;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthGroupScopesUpdater {
	public static void setCurrentGroup(String group){
		FurmsOAuth2User principal = (FurmsOAuth2User)SecurityContextHolder.getContext()
			.getAuthentication()
			.getPrincipal();
		principal.currentGroup = group;
	}
}
