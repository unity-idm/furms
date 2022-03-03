/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.domain.authz.roles.Capability;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
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

	/**
	 * Community Admin should have access to all projects own by his community.
	 * To implement this requirement method has to check if user is Community Admin and then if community owns project.
	 * If so method returns community ResourceId instead of project ResourceId.
	 */
	private ResourceId getCommunityResourceIdAssociatedWithProject(Map<ResourceId, Set<Role>> roles, ResourceId projectId) {
		Set<String> communityIds = roles.entrySet().stream()
			.filter(role -> role.getValue().contains(Role.COMMUNITY_ADMIN))
			.map(Map.Entry::getKey)
			.filter(rId -> rId.type.equals(COMMUNITY))
			.map(x -> x.id.toString())
			.collect(Collectors.toSet());

		return mapProjectToCommunityResourceId(communityIds)
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
			.getOrDefault(projectId, projectId);
	}

	private Stream<Map.Entry<ResourceId, ResourceId>> mapProjectToCommunityResourceId(Set<String> communityResourceIds) {
		return projectRepository.findAllByCommunityIds(communityResourceIds).stream()
			.map(project -> Map.entry(new ResourceId(project.getId(), PROJECT), new ResourceId(project.getCommunityId(), COMMUNITY)));
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
