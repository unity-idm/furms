/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
class CommunityServiceImp implements CommunityService {
	private final CommunityRepository communityRepository;
	private final CommunityGroupsDAO communityGroupsDAO;
	private final CommunityServiceValidator validator;

	CommunityServiceImp(CommunityRepository communityRepository,
	                    CommunityGroupsDAO communityGroupsDAO,
	                    CommunityServiceValidator validator) {
		this.communityRepository = communityRepository;
		this.communityGroupsDAO = communityGroupsDAO;
		this.validator = validator;
	}

	@Override
	public Optional<Community> findById(String id) {
		return communityRepository.findById(id);
	}

	@Override
	public Set<Community> findAll() {
		return communityRepository.findAll();
	}

	@Override
	@Transactional
	public void create(Community community) {
		validator.validateCreate(community);
		String id = communityRepository.create(community);
		communityGroupsDAO.create(new CommunityGroup(id, community.getName()));
	}

	@Override
	@Transactional
	public void update(Community community) {
		validator.validateUpdate(community);
		communityRepository.update(community);
		communityGroupsDAO.update(new CommunityGroup(community.getId(), community.getName()));
	}

	@Override
	@Transactional
	public void delete(String id) {
		validator.validateDelete(id);
		communityRepository.delete(id);
		communityGroupsDAO.delete(id);
	}
}
