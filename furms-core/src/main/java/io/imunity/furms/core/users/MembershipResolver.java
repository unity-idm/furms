/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.core.users;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.UserAttribute;
import io.imunity.furms.domain.users.CommunityMembership;
import io.imunity.furms.domain.users.ProjectMembership;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;

@Service
class MembershipResolver {
	private static final Logger LOG = LoggerFactory.getLogger(MembershipResolver.class);
	private static final String EXCLUDED_ATTR_PREFIX = "sys:";
	private final CommunityRepository communitiesDAO;
	private final ProjectRepository projectsDAO;
	
	MembershipResolver(CommunityRepository communitiesDAO, ProjectRepository projectsDAO) {
		this.communitiesDAO = communitiesDAO;
		this.projectsDAO = projectsDAO;
	}
	
	Set<UserAttribute> filterExposedAttribtues(Set<UserAttribute> allAttribtues) {
		return allAttribtues.stream()
				.filter(exposedAttributePredicate())
				.collect(Collectors.toSet());
	}
	
	Set<CommunityMembership> resolveCommunitiesMembership(Map<ResourceId, Set<UserAttribute>> attributesByResource) {
		return attributesByResource.keySet().stream()
				.filter(resId -> resId.type == ResourceType.COMMUNITY)
				.map(resId -> resolveCommunityMembership(resId, attributesByResource))
				.filter(memOpt -> memOpt.isPresent())
				.map(memOpt -> memOpt.get())
				.collect(Collectors.toSet());
	}
	
	private Optional<CommunityMembership> resolveCommunityMembership(ResourceId community, 
			Map<ResourceId, Set<UserAttribute>> attributesByResource) {
		Optional<Community> communityOpt = communitiesDAO.findById(community.id.toString());
		if (communityOpt.isEmpty()) {
			LOG.warn("Community {} is defined in users directory (unity) but not in FURMS DB", community.id);
			return Optional.empty();
		}
		Set<ProjectMembership> projectsMembership = resolveProjectsMembership(community, attributesByResource);
		Set<UserAttribute> communityAttributes = filterExposedAttribtues(
				attributesByResource.getOrDefault(community, Collections.emptySet()));
		return Optional.of(new CommunityMembership(community.id.toString(), communityOpt.get().getName(), 
				projectsMembership, communityAttributes));
	}

	private Set<ProjectMembership> resolveProjectsMembership(ResourceId community, 
			Map<ResourceId, Set<UserAttribute>> attributesByResource) {
		Map<String, Project> communityProjects = projectsDAO.findAllByCommunityId(community.id.toString()).stream()
				.collect(Collectors.toMap(proj -> proj.getId(), proj -> proj));
		return attributesByResource.entrySet().stream()
			.filter(entry -> entry.getKey().type == ResourceType.PROJECT)
			.filter(entry -> communityProjects.containsKey(entry.getKey().id.toString()))
			.map(entry -> resolveProjectMembership(entry.getValue(), 
					communityProjects.get(entry.getKey().id.toString())))
			.collect(Collectors.toSet());
	}

	private ProjectMembership resolveProjectMembership(Set<UserAttribute> attributes, Project project) {
		return new ProjectMembership(project.getId(), project.getName(), filterExposedAttribtues(attributes));
	}
	
	private Predicate<UserAttribute> exposedAttributePredicate() {
		return a -> !a.name.startsWith(EXCLUDED_ATTR_PREFIX);
	}
	
}
