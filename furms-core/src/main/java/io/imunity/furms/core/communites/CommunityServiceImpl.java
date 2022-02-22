/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.domain.authz.roles.Capability;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityCreatedEvent;
import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.communities.CommunityRemovedEvent;
import io.imunity.furms.domain.communities.CommunityUpdatedEvent;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.AllUsersAndCommunityAdmins;
import io.imunity.furms.domain.users.CommunityUsersAndCommunityAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserRoleGrantedEvent;
import io.imunity.furms.domain.users.UserRoleRevokedEvent;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.Role.COMMUNITY_ADMIN;
import static java.util.stream.Collectors.toSet;

@Service
class CommunityServiceImpl implements CommunityService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final CommunityRepository communityRepository;
	private final CommunityGroupsDAO communityGroupsDAO;
	private final CommunityServiceValidator validator;
	private final ApplicationEventPublisher publisher;
	private final AuthzService authzService;
	private final CapabilityCollector capabilityCollector;
	private final InvitatoryService invitatoryService;

	CommunityServiceImpl(CommunityRepository communityRepository,
	                     CommunityGroupsDAO communityGroupsDAO,
	                     CommunityServiceValidator validator,
	                     AuthzService authzService,
	                     ApplicationEventPublisher publisher,
	                     CapabilityCollector capabilityCollector,
						 InvitatoryService invitatoryService) {
		this.communityRepository = communityRepository;
		this.communityGroupsDAO = communityGroupsDAO;
		this.validator = validator;
		this.publisher = publisher;
		this.authzService = authzService;
		this.capabilityCollector = capabilityCollector;
		this.invitatoryService = invitatoryService;
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED)
	public boolean existsById(String id) {
		return communityRepository.exists(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "ids", idCollections = true)
	public Set<Community> findAll(Set<String> ids) {
		return communityRepository.findAll(ids);
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
	@FurmsAuthorize(capability = AUTHENTICATED)
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
		Community created = communityRepository.findById(id).get();
		communityGroupsDAO.create(new CommunityGroup(id, community.getName()));
		LOG.info("Community with given ID: {} was created: {}", id, community);
		publisher.publishEvent(new CommunityCreatedEvent(created));
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "community.id")
	public void update(Community community) {
		validator.validateUpdate(community);
		Community oldCommunity = communityRepository.findById(community.getId()).get();
		communityRepository.update(community);
		communityGroupsDAO.update(new CommunityGroup(community.getId(), community.getName()));
		LOG.info("Community was updated: {}", community);
		publisher.publishEvent(new CommunityUpdatedEvent(oldCommunity, community));
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY)
	public void delete(String id) {
		validator.validateDelete(id);
		Community community = communityRepository.findById(id).get();
		communityRepository.delete(id);
		communityGroupsDAO.delete(id);
		LOG.info("Community with given ID: {} was deleted", id);
		publisher.publishEvent(new CommunityRemovedEvent(community));
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id="id")
	public List<FURMSUser> findAllAdmins(String id) {
		return communityGroupsDAO.getAllAdmins(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id="id")
	public CommunityUsersAndCommunityAdmins findAllCommunityAdminsAllUsers(String id) {
		return communityGroupsDAO.getCommunityAdminsAndUsers(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id="id")
	public List<FURMSUser> findAllUsers(String id) {
		return communityGroupsDAO.getAllUsers(id).stream()
			.filter(furmsUser -> furmsUser.fenixUserId.isPresent())
			.collect(Collectors.toList());
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY)
	public AllUsersAndCommunityAdmins findAllAdminsWithAllUsers(String id) {
		return communityGroupsDAO.getAllUsersAndCommunityAdmins(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id="communityId")
	public Set<Invitation> findAllInvitations(String communityId) {
		return invitatoryService.getInvitations(COMMUNITY_ADMIN, UUID.fromString(communityId));
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id="communityId")
	public void inviteAdmin(String communityId, PersistentId userId) {
		communityRepository.findById(communityId).ifPresent(community ->
			invitatoryService.inviteUser(userId, new ResourceId(communityId, COMMUNITY), COMMUNITY_ADMIN, community.getName())
		);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id="communityId")
	public void inviteAdmin(String communityId, String email) {
		communityRepository.findById(communityId).ifPresent(community ->
			invitatoryService.inviteUser(email, new ResourceId(communityId, COMMUNITY), COMMUNITY_ADMIN, community.getName())
		);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id="communityId")
	public void resendInvitation(String communityId, InvitationId invitationId) {
		if(!invitatoryService.checkAssociation(communityId, invitationId))
			throw new IllegalArgumentException(String.format("Invitation %s is not associate with this resource %s", communityId, invitationId));
		invitatoryService.resendInvitation(invitationId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id="communityId")
	public void removeInvitation(String communityId, InvitationId invitationId) {
		if(!invitatoryService.checkAssociation(communityId, invitationId))
			throw new IllegalArgumentException(String.format("Invitation %s is not associate with this resource %s", communityId, invitationId));
		invitatoryService.removeInvitation(invitationId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id="communityId")
	public void addAdmin(String communityId, PersistentId userId) {
		communityGroupsDAO.addAdmin(communityId, userId);
		LOG.info("Added Site Administrator ({}) in Unity for Site ID={}", userId, communityId);
		String communityName = communityRepository.findById(communityId).get().getName();
		publisher.publishEvent(new UserRoleGrantedEvent(userId,  new ResourceId(communityId, COMMUNITY), communityName, COMMUNITY_ADMIN));
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id="communityId")
	public void removeAdmin(String communityId, PersistentId userId) {
		communityGroupsDAO.removeAdmin(communityId, userId);
		String communityName = communityRepository.findById(communityId).get().getName();
		LOG.info("Removed Community Administrator ({}) from Unity for Site ID={}", userId, communityId);
		publisher.publishEvent(new UserRoleRevokedEvent(userId,  new ResourceId(communityId, COMMUNITY), communityName, COMMUNITY_ADMIN));
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
