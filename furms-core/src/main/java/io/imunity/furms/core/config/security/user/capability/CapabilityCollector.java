/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user.capability;


import io.imunity.furms.domain.authz.roles.Capability;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static java.util.stream.Collectors.toMap;

@Component
public class CapabilityCollector {
	private final ProjectRepository projectRepository;

	public CapabilityCollector(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	public Set<Capability> getCapabilities(Map<ResourceId, Set<Role>> roles, ResourceId resourceId){
		Set<Capability> capabilities = getAdditionalCapabilities(roles);

		resourceId = handleCommunityAdminProjectResources(roles, resourceId);

		for(Role role: roles.getOrDefault(resourceId, new HashSet<>())){
			capabilities.addAll(role.capabilities);
		}
		return capabilities;
	}

	private ResourceId handleCommunityAdminProjectResources(Map<ResourceId, Set<Role>> roles, ResourceId resourceId) {
		Map<ResourceId, ResourceId> projectIds = roles.keySet().stream()
			.filter(rId -> rId.type.equals(COMMUNITY))
			.flatMap(this::mapResources)
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		return projectIds.getOrDefault(resourceId, resourceId);
	}

	private Stream<Map.Entry<ResourceId, ResourceId>> mapResources(ResourceId resourceId) {
		return projectRepository.findAll(resourceId.id.toString()).stream()
			.map(Project::getId)
			.map(id -> new ResourceId(id, PROJECT))
			.map(rId -> Map.entry(rId, resourceId));
	}

	private Set<Capability> getAdditionalCapabilities(Map<ResourceId, Set<Role>> resourceIdToRoles) {
		Set<Capability> capabilities = new HashSet<>();
		Set<Role> roles = new HashSet<>();

		for(Set<Role> role: resourceIdToRoles.values()){
			roles.addAll(role);
		}
		for(Role r : roles){
			capabilities.addAll(r.additionalCapabilities);
		}
		return capabilities;
	}
}
