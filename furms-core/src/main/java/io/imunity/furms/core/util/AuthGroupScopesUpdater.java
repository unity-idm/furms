/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.util;

import io.imunity.furms.core.config.security.user.FurmsUserContext;
import io.imunity.furms.core.config.security.user.ResourceId;
import io.imunity.furms.core.config.security.user.ViewContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthGroupScopesUpdater {
	public static void setCurrentGroup(ViewContext viewContext){
		FurmsUserContext principal = (FurmsUserContext)SecurityContextHolder.getContext()
			.getAuthentication()
			.getPrincipal();
		principal.viewContext = viewContext;
	}

	public static void setResourceId(ResourceId resourceId){
		FurmsUserContext principal = (FurmsUserContext)SecurityContextHolder.getContext()
			.getAuthentication()
			.getPrincipal();
		principal.viewContext = new ViewContext(null, resourceId);
	}

//	@PreAuthorize("hasCapability('SITE_READ', SITE, #siteId)")
//	public Site getSite(UUID siteId){
//		siteRepository.findSite(siteId);
//	}
//
//	@PreAuthorize("hasCapability('SITE_READ', SYSTEM_LEVEL)")
//	public Site getSites(){
//		siteRepository.findAll();
//	}
}
