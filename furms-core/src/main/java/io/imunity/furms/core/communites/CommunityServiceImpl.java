/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;

@Service
class CommunityServiceImpl implements CommunityService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final CommunityRepository communityRepository;
	private final CommunityGroupsDAO communityGroupsDAO;
	private final CommunityServiceValidator validator;

	CommunityServiceImpl(CommunityRepository communityRepository,
	                     CommunityGroupsDAO communityGroupsDAO,
	                     CommunityServiceValidator validator) {
		this.communityRepository = communityRepository;
		this.communityGroupsDAO = communityGroupsDAO;
		this.validator = validator;
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "id")
	public Optional<Community> findById(String id) {
		return communityRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY)
	public Set<Community> findAll() {
		return communityRepository.findAll();
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public void create(Community community) {
		validator.validateCreate(community);
		String id = communityRepository.create(community);
		communityGroupsDAO.create(new CommunityGroup(id, community.getName()));
		LOG.info("Community with given ID: {} was created: {}", id, community);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "community.id")
	public void update(Community community) {
		validator.validateUpdate(community);
		communityRepository.update(community);
		communityGroupsDAO.update(new CommunityGroup(community.getId(), community.getName()));
		LOG.info("Community was updated: {}", community);

	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public void delete(String id) {
		validator.validateDelete(id);
		communityRepository.delete(id);
		communityGroupsDAO.delete(id);
		LOG.info("Community with given ID: {} was deleted", id);
	}
}
