/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.db;

import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.services.ServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepoCleaner {
	
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private ServiceRepository serviceRepository;
	
	public void cleanAll() {
		serviceRepository.deleteAll();
		projectRepository.deleteAll();
		communityRepository.deleteAll();
		siteRepository.deleteAll();
	}
}
