/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.domain.authz.roles.Capability;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static java.util.stream.Collectors.toMap;

@Component
class UserCapabilityCollector implements CapabilityCollector {
	private final ProjectRepository projectRepository;

	public UserCapabilityCollector(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	public Set<Capability> getCapabilities(Map<ResourceId, Set<Role>> roles, ResourceId resourceId) {
		Set<Capability> capabilities = getGlobalCapabilities(roles);
		if(resourceId.type.equals(PROJECT))
			resourceId = getCommunityResourceIdAssociatedWithProject(roles, resourceId);

		for(Role role: roles.getOrDefault(resourceId, new HashSet<>())){
			capabilities.addAll(role.capabilities);
		}
		return capabilities;
	}

	public Set<Capability> getCapabilities(Map<ResourceId, Set<Role>> roles, List<ResourceId> resourceId,
	                                       ResourceType type) {
		Set<Capability> capabilities = getGlobalCapabilities(roles);
		if(type.equals(PROJECT)) {
			Set<Project> all = projectRepository.findAll();
			resourceId = getCommunityResourceIdAssociatedWithProject(roles, resourceId, all);
		}

		for(ResourceId resourceId1: resourceId){
			for(Role role: roles.getOrDefault(resourceId1, new HashSet<>())){
				capabilities.addAll(role.capabilities);
			}
		}

		return capabilities;
	}

	/**
	 * Community Admin should have access to all projects own by his community.
	 * To implement this requirement method has to check if user is Community Admin and then if community owns project.
	 * If so method returns community ResourceId instead of project ResourceId.
	 */
	private ResourceId getCommunityResourceIdAssociatedWithProject(Map<ResourceId, Set<Role>> roles, ResourceId projectId) {
		Set<CommunityId> communityIds = roles.entrySet().stream()
			.filter(role -> role.getValue().contains(Role.COMMUNITY_ADMIN))
			.map(Map.Entry::getKey)
			.filter(rId -> rId.type.equals(COMMUNITY))
			.map(resourceId -> (CommunityId)resourceId.id)
			.collect(Collectors.toSet());

		return mapProjectToCommunityResourceId(communityIds)
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
			.getOrDefault(projectId, projectId);
	}

	private List<ResourceId> getCommunityResourceIdAssociatedWithProject(Map<ResourceId, Set<Role>> roles,
	                                                               List<ResourceId> projectIds, Set<Project> projects) {
		Set<CommunityId> communityIds = roles.entrySet().stream()
			.filter(role -> role.getValue().contains(Role.COMMUNITY_ADMIN))
			.map(Map.Entry::getKey)
			.filter(rId -> rId.type.equals(COMMUNITY))
			.map(resourceId -> (CommunityId)resourceId.id)
			.collect(Collectors.toSet());

		Map<ResourceId, ResourceId> projectToCommunity = projects.stream()
			.filter(project -> communityIds.contains(project.getCommunityId()))
			.map(project -> Map.entry(
				new ResourceId(project.getId(), PROJECT),
				new ResourceId(project.getCommunityId(), COMMUNITY))
			)
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		return projectIds.stream()
			.map(x -> projectToCommunity.getOrDefault(x, x))
			.collect(Collectors.toList());
	}

	private Stream<Map.Entry<ResourceId, ResourceId>> mapProjectToCommunityResourceId(Set<CommunityId> communityResourceIds) {
		return projectRepository.findAllByCommunityIds(communityResourceIds).stream()
			.map(project -> Map.entry(
				new ResourceId(project.getId(), PROJECT),
				new ResourceId(project.getCommunityId(), COMMUNITY))
			);
	}

	private Set<Capability> getGlobalCapabilities(Map<ResourceId, Set<Role>> resourceIdToRoles) {
		Set<Capability> capabilities = new HashSet<>();
		Set<Role> roles = new HashSet<>();

		for(Set<Role> role: resourceIdToRoles.values()){
			roles.addAll(role);
		}
		for(Role r : roles){
			capabilities.addAll(r.globalCapabilities);
		}

		return capabilities;
	}
}
