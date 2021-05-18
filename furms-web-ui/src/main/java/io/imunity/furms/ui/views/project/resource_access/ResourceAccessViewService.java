/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.resource_access;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.resource_access.AccessStatus.*;
import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

class ResourceAccessViewService {
	public final String projectId;
	public final String communityId;

	public final ProjectService projectService;
	public final ProjectAllocationService projectAllocationService;
	public final ResourceAccessService resourceAccessService;

	public Map<ResourceAccessModel, List<ResourceAccessModel>> data;
	public Map<Pair<String, String>, UserGrant> usersGrants;
	public Set<String> addedUsersIds;

	ResourceAccessViewService(ProjectService projectService, ProjectAllocationService projectAllocationService, ResourceAccessService resourceAccessService, String projectId) {
		this.projectId = projectId;
		this.communityId = projectService.findById(projectId).get().getCommunityId();

		this.projectService = projectService;
		this.projectAllocationService = projectAllocationService;
		this.resourceAccessService = resourceAccessService;

		reloadUserGrants();
	}

	public void reloadUserGrants() {
		this.usersGrants = loadUsersGrants();
		this.addedUsersIds = resourceAccessService.findAddedUser(projectId);
		this.data = loadData();
	}

	private Map<Pair<String, String>, UserGrant> loadUsersGrants() {
		return resourceAccessService.findUsersGrants(projectId).stream()
			.collect(toMap(x -> Pair.of(x.userId, x.projectAllocationId), x -> x));
	}

	public boolean isGrantOrRevokeAvailable(ResourceAccessModel resourceAccessModel) {
		UserGrant userGrant = usersGrants.get(Pair.of(resourceAccessModel.getFenixUserId(), resourceAccessModel.getAllocationId()));
		return resourceAccessModel.getFirstName() == null &&
			!resourceAccessModel.isAccessible() &&
			addedUsersIds.contains(resourceAccessModel.getFenixUserId().id) &&
			Optional.ofNullable(userGrant).filter(x -> PENDING_AND_ACKNOWLEDGED_STATUES.contains(x.status)).isEmpty();
	}

	public boolean isRevokeAvailable(ResourceAccessModel resourceAccessModel) {
		UserGrant userGrant = usersGrants.get(Pair.of(resourceAccessModel.getFenixUserId(), resourceAccessModel.getAllocationId()));
		return userGrant != null && TERMINAL_GRANTED.contains(userGrant.status);
	}

	public void grantAccess(ResourceAccessModel resourceAccessModel) {
		resourceAccessService.grantAccess(
			GrantAccess.builder()
				.siteId(resourceAccessModel.getSiteId())
				.projectId(projectId)
				.allocationId(resourceAccessModel.getAllocationId())
				.fenixUserId(resourceAccessModel.getFenixUserId())
				.build()
		);
	}

	public void revokeAccess(ResourceAccessModel resourceAccessModel) {
		resourceAccessService.revokeAccess(
			GrantAccess.builder()
				.siteId(resourceAccessModel.getSiteId())
				.projectId(projectId)
				.allocationId(resourceAccessModel.getAllocationId())
				.fenixUserId(resourceAccessModel.getFenixUserId())
				.build()
		);
	}

	Map<ResourceAccessModel, List<ResourceAccessModel>> loadDataWithFilters(String value, Set<String> allSelectedItems) {
		return data.entrySet().stream()
			.filter(entry -> value.isEmpty() || entry.getKey().matches(value))
			.collect(toMap(
				Map.Entry::getKey,
				x -> x.getValue().stream()
					.filter(resourceAccessModel -> allSelectedItems.isEmpty() || allSelectedItems.contains(resourceAccessModel.getAllocation()))
					.collect(toList()))
			);
	}

	public Map<ResourceAccessModel, List<ResourceAccessModel>> getData(){
		return data;
	}

	public Set<String> getAllocations() {
		return projectAllocationService.findAll(communityId, projectId).stream()
			.map(x -> x.name)
			.collect(Collectors.toSet());
	}

	public Map<ResourceAccessModel, List<ResourceAccessModel>> loadData() {
		Set<ProjectAllocationResolved> allocations = projectAllocationService.findAllWithRelatedObjects(communityId, projectId);
		return projectService.findAllUsers(communityId, projectId).stream()
			.collect(Collectors.toMap(u ->
					ResourceAccessModel.builder()
						.firstName(u.firstName.orElse(""))
						.lastName(u.lastName.orElse(""))
						.email(u.email)
						.build(),
				u -> allocations.stream()
					.map(allocation -> ResourceAccessModel.builder()
						.allocation(allocation.name)
						.access(getEnabledValue(u, allocation))
						.status(getStatusValue(u, allocation))
						.siteId(new SiteId(allocation.site.getId(), allocation.site.getExternalId()))
						.allocationId(allocation.id)
						.accessible(allocation.resourceType.accessibleForAllProjectMembers)
						.fenixUserId(u.fenixUserId.orElseGet(FenixUserId::empty))
						.message(getMessage(u, allocation))
						.build())
					.collect(toList())
			));
	}

	private String getStatusValue(FURMSUser user, ProjectAllocationResolved allocation) {
		if(allocation.resourceType.accessibleForAllProjectMembers)
			return getTranslation("view.project-admin.resource-access.grid.status.applied");
		UserGrant userGrant = usersGrants.get(Pair.of(user.fenixUserId.get().id, allocation.id));
		if(userGrant == null)
			return "-";
		if(PENDING_STATUES.contains(userGrant.status))
			return getTranslation("view.project-admin.resource-access.grid.status.pending");
		if(ACKNOWLEDGED_STATUES.contains(userGrant.status))
			return getTranslation("view.project-admin.resource-access.grid.status.acknowledged");
		if(GRANTED.equals(userGrant.status))
			return getTranslation("view.project-admin.resource-access.grid.status.applied");
		if(FAILED_STATUES.contains(userGrant.status))
			return getTranslation("view.project-admin.resource-access.grid.status.failed");
		return "-";
	}

	private String getEnabledValue(FURMSUser user, ProjectAllocationResolved allocation) {
		if(allocation.resourceType.accessibleForAllProjectMembers)
			return getTranslation("view.project-admin.resource-access.grid.access.enabled");
		UserGrant userGrant = usersGrants.get(Pair.of(user.fenixUserId.get().id, allocation.id));
		if(userGrant != null && ENABLED_STATUES.contains(userGrant.status))
			return getTranslation("view.project-admin.resource-access.grid.access.enabled");
		return getTranslation("view.project-admin.resource-access.grid.access.disabled");
	}

	private String getMessage(FURMSUser user, ProjectAllocationResolved allocation){
		UserGrant userGrant = usersGrants.get(Pair.of(user.fenixUserId.get().id, allocation.id));
		return Optional.ofNullable(userGrant)
			.flatMap(x -> x.errorMessage)
			.map(x -> x.message)
			.orElse(null);
	}
}
