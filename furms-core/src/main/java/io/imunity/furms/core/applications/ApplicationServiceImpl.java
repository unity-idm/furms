/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.applications;

import io.imunity.furms.api.applications.ApplicationService;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.applications.ApplicationRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static java.util.stream.Collectors.toList;

@Service
class ApplicationServiceImpl implements ApplicationService {

	private final ApplicationRepository applicationRepository;
	private final UsersDAO usersDAO;
	private final ProjectGroupsDAO projectGroupsDAO;
	private final ProjectRepository projectRepository;
	private final AuthzService authzService;

	ApplicationServiceImpl(ApplicationRepository applicationRepository, UsersDAO usersDAO,
	                       ProjectGroupsDAO projectGroupsDAO, ProjectRepository projectRepository,
	                       AuthzService authzService) {
		this.applicationRepository = applicationRepository;
		this.usersDAO = usersDAO;
		this.projectGroupsDAO = projectGroupsDAO;
		this.projectRepository = projectRepository;
		this.authzService = authzService;
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id="projectId")
	public List<FURMSUser> findAllApplyingUsers(String projectId) {
		Set<FenixUserId> usersIds = applicationRepository.findAllApplyingUsers(projectId);
		return usersDAO.getAllUsers().stream()
			.filter(usr -> usr.fenixUserId.isPresent())
			.filter(usr -> usersIds.contains(usr.fenixUserId.get()))
			.collect(toList());
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public Set<String> findAllAppliedProjectsIdsForCurrentUser() {
		FenixUserId fenixUserId = authzService.getCurrentAuthNUser().fenixUserId
			.orElseThrow(UserWithoutFenixIdValidationError::new);
		return applicationRepository.findAllAppliedProjectsIds(fenixUserId);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public void createForCurrentUser(String projectId) {
		FenixUserId fenixUserId = authzService.getCurrentAuthNUser().fenixUserId
			.orElseThrow(UserWithoutFenixIdValidationError::new);
		applicationRepository.create(projectId, fenixUserId);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public void removeForCurrentUser(String projectId) {
		FenixUserId fenixUserId = authzService.getCurrentAuthNUser().fenixUserId
			.orElseThrow(UserWithoutFenixIdValidationError::new);
		applicationRepository.remove(projectId, fenixUserId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId")
	public void accept(String projectId, FenixUserId fenixUserId) {
		if(applicationRepository.existsBy(projectId, fenixUserId)) {
			projectRepository.findById(projectId).ifPresent(project -> {
				String communityId = project.getCommunityId();
				PersistentId persistentId = usersDAO.getPersistentId(fenixUserId);
				projectGroupsDAO.addProjectUser(communityId, projectId, persistentId, Role.PROJECT_USER);
				applicationRepository.remove(projectId, fenixUserId);
			});
		}
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId")
	public void remove(String projectId, FenixUserId fenixUserId) {
		applicationRepository.remove(projectId, fenixUserId);
	}
}
