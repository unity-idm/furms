/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.Role.COMMUNITY_ADMIN;
import static java.util.stream.Collectors.toSet;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.domain.authz.roles.Capability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.communities.CreateCommunityEvent;
import io.imunity.furms.domain.communities.RemoveCommunityEvent;
import io.imunity.furms.domain.communities.UpdateCommunityEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.invitations.InviteUserEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.RemoveUserRoleEvent;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.users.UsersDAO;

@Service
class CommunityServiceImpl implements CommunityService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final CommunityRepository communityRepository;
	private final CommunityGroupsDAO communityGroupsDAO;
	private final UsersDAO usersDAO;
	private final CommunityServiceValidator validator;
	private final ApplicationEventPublisher publisher;
	private final AuthzService authzService;
	private final CapabilityCollector capabilityCollector;

	CommunityServiceImpl(CommunityRepository communityRepository,
	                     CommunityGroupsDAO communityGroupsDAO,
	                     UsersDAO usersDAO,
	                     CommunityServiceValidator validator,
	                     AuthzService authzService,
	                     ApplicationEventPublisher publisher,
	                     CapabilityCollector capabilityCollector) {
		this.communityRepository = communityRepository;
		this.communityGroupsDAO = communityGroupsDAO;
		this.usersDAO = usersDAO;
		this.validator = validator;
		this.publisher = publisher;
		this.authzService = authzService;
		this.capabilityCollector = capabilityCollector;
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public boolean existsById(String id) {
		return communityRepository.exists(id);
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
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public Set<Community> findAllOfCurrentUser() {
		final FURMSUser currentUser = authzService.getCurrentAuthNUser();
		return communityRepository.findAll().stream()
				.filter(community -> isBelongToCommunity(community, currentUser))
				.collect(toSet());
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public void create(Community community) {
		validator.validateCreate(community);
		String id = communityRepository.create(community);
		communityGroupsDAO.create(new CommunityGroup(id, community.getName()));
		LOG.info("Community with given ID: {} was created: {}", id, community);
		publisher.publishEvent(new CreateCommunityEvent(community.getId()));
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "community.id")
	public void update(Community community) {
		validator.validateUpdate(community);
		communityRepository.update(community);
		communityGroupsDAO.update(new CommunityGroup(community.getId(), community.getName()));
		LOG.info("Community was updated: {}", community);
		publisher.publishEvent(new UpdateCommunityEvent(community.getId()));
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public void delete(String id) {
		validator.validateDelete(id);
		communityRepository.delete(id);
		communityGroupsDAO.delete(id);
		LOG.info("Community with given ID: {} was deleted", id);
		publisher.publishEvent(new RemoveCommunityEvent(id));
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id="id")
	public List<FURMSUser> findAllAdmins(String id) {
		return communityGroupsDAO.getAllAdmins(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id="communityId")
	public void inviteAdmin(String communityId, PersistentId userId) {
		Optional<FURMSUser> user = usersDAO.findById(userId);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("Could not invite user due to wrong email address.");
		}
		communityGroupsDAO.addAdmin(communityId, userId);
		LOG.info("Added Site Administrator ({}) in Unity for Site ID={}", userId, communityId);
		publisher.publishEvent(new InviteUserEvent(userId, new ResourceId(communityId, COMMUNITY)));
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id="communityId")
	public void addAdmin(String communityId, PersistentId userId) {
		communityGroupsDAO.addAdmin(communityId, userId);
		LOG.info("Added Site Administrator ({}) in Unity for Site ID={}", userId, communityId);
		publisher.publishEvent(new InviteUserEvent(userId, new ResourceId(communityId, COMMUNITY)));
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id="communityId")
	public void removeAdmin(String communityId, PersistentId userId) {
		communityGroupsDAO.removeAdmin(communityId, userId);
		LOG.info("Removed Site Administrator ({}) from Unity for Site ID={}", userId, communityId);
		publisher.publishEvent(new RemoveUserRoleEvent(userId,  new ResourceId(communityId, COMMUNITY)));
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id="communityId")
	public boolean isAdmin(String communityId) {
		return authzService.isResourceMember(communityId, COMMUNITY_ADMIN);
	}

	private boolean isBelongToCommunity(Community community, FURMSUser user) {
		final Set<Capability> capabilities = Set.of(COMMUNITY_READ, COMMUNITY_WRITE);
		return capabilityCollector.getCapabilities(user.roles, new ResourceId(community.getId(), COMMUNITY)).stream()
				.anyMatch(capabilities::contains);
	}
}
