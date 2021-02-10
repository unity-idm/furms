/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.*;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;

@Service
class CommunityServiceImpl implements CommunityService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final CommunityRepository communityRepository;
	private final CommunityGroupsDAO communityGroupsDAO;
	private final UsersDAO usersDAO;
	private final CommunityServiceValidator validator;

	CommunityServiceImpl(CommunityRepository communityRepository, CommunityGroupsDAO communityGroupsDAO,
	                            UsersDAO usersDAO, CommunityServiceValidator validator) {
		this.communityRepository = communityRepository;
		this.communityGroupsDAO = communityGroupsDAO;
		this.usersDAO = usersDAO;
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

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id="id")
	public List<User> findAllAdmins(String id) {
		return communityGroupsDAO.getAllAdmins(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id="siteId")
	public void inviteAdmin(String communityId, String email) {
		Optional<User> user = usersDAO.findByEmail(email);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("Could not invite user due to wrong email address.");
		}
		addAdmin(communityId, user.get().id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id="communityId")
	public void addAdmin(String communityId, String userId) {
		communityGroupsDAO.addAdmin(communityId, userId);
		LOG.info("Added Site Administrator ({}) in Unity for Site ID={}", userId, communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id="communityId")
	public void removeAdmin(String communityId, String userId) {
		communityGroupsDAO.removeAdmin(communityId, userId);
		LOG.info("Removed Site Administrator ({}) from Unity for Site ID={}", userId, communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id="communityId")
	public boolean isAdmin(String communityId, String userId) {
		return communityGroupsDAO.isAdmin(communityId, userId);
	}
}
