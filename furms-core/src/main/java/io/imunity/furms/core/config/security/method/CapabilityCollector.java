/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

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

import static io.imunity.furms.domain.authz.roles.Capability.REST_API_CALL;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.domain.authz.roles.Role.hasAdminRole;
import static java.util.stream.Collectors.toMap;

@Component
class CapabilityCollector {
	private final ProjectRepository projectRepository;

	public CapabilityCollector(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	public Set<Capability> getCapabilities(Map<ResourceId, Set<Role>> roles, ResourceId resourceId){
		Set<Capability> capabilities = getAdditionalCapabilities(roles);

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
		Map<ResourceId, ResourceId> projectToCommunity = roles.entrySet().stream()
			.filter(role -> role.getValue().contains(Role.COMMUNITY_ADMIN))
			.map(Map.Entry::getKey)
			.filter(rId -> rId.type.equals(COMMUNITY))
			.flatMap(this::mapProjectToCommunityResourceId)
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		return projectToCommunity.getOrDefault(projectId, projectId);
	}

	private Stream<Map.Entry<ResourceId, ResourceId>> mapProjectToCommunityResourceId(ResourceId communityResourceId) {
		return projectRepository.findAllByCommunityId(communityResourceId.id.toString()).stream()
			.map(Project::getId)
			.map(id -> new ResourceId(id, PROJECT))
			.map(rId -> Map.entry(rId, communityResourceId));
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

		insertRestApiCallCapability(capabilities, roles);

		return capabilities;
	}

	private void insertRestApiCallCapability(Set<Capability> capabilities, Set<Role> roles) {
		if (hasAdminRole(roles)) {
			capabilities.add(REST_API_CALL);
		}
	}
}
