/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateJob;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectOperationService;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.imunity.furms.core.utils.AfterCommitLauncher.runAfterCommit;

@Service
class ProjectInstallationServiceImpl implements ProjectInstallationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectOperationRepository projectOperationRepository;
	private final SiteAgentProjectOperationService siteAgentProjectOperationService;
	private final UsersDAO usersDAO;
	private final SiteRepository siteRepository;
	private final CommunityRepository communityRepository;

	ProjectInstallationServiceImpl(ProjectOperationRepository projectOperationRepository,
	                               SiteAgentProjectOperationService siteAgentProjectOperationService,
	                               UsersDAO usersDAO, SiteRepository siteRepository, CommunityRepository communityRepository) {
		this.projectOperationRepository = projectOperationRepository;
		this.siteAgentProjectOperationService = siteAgentProjectOperationService;
		this.usersDAO = usersDAO;
		this.siteRepository = siteRepository;
		this.communityRepository = communityRepository;
	}

	@Override
	public ProjectInstallation findProjectInstallationOfProjectAllocation(String projectAllocationId) {
		return projectOperationRepository.findProjectInstallation(projectAllocationId, usersDAO::findById);
	}

	@Override
	public boolean isProjectInstalled(String siteId, String projectId) {
		return projectOperationRepository.installedProjectExistsBySiteIdAndProjectId(siteId, projectId);
	}

	@Override
	public boolean isProjectInTerminalState(String projectId) {
		return projectOperationRepository.areAllProjectOperationInTerminateState(projectId);
	}

	@Override
	@Transactional
	public void createOrUpdate(String projectId, ProjectInstallation projectInstallation) {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectInstallationJob projectInstallationJob = ProjectInstallationJob.builder()
			.correlationId(correlationId)
			.siteId(projectInstallation.siteId)
			.projectId(projectId)
			.status(ProjectInstallationStatus.PENDING)
			.build();

		projectOperationRepository.createOrUpdate(projectInstallationJob);
		runAfterCommit(() ->
			siteAgentProjectOperationService.installProject(projectInstallationJob.correlationId, projectInstallation)
		);
		LOG.info("ProjectInstallation was created: {}", projectInstallationJob);
	}

	private void update(SiteId siteId, Project project) {
		FURMSUser leader = project.getLeaderId() != null
			? usersDAO.findById(project.getLeaderId()).orElse(null)
			: null;
		Community community = communityRepository.findById(project.getCommunityId())
			.get();
		ProjectInstallation projectInstallation = ProjectInstallation.builder()
			.id(project.getId())
			.siteId(siteId.id)
			.siteExternalId(siteId.externalId.id)
			.name(project.getName())
			.description(project.getDescription())
			.communityId(project.getCommunityId())
			.communityName(community.getName())
			.acronym(project.getAcronym())
			.researchField(project.getResearchField())
			.validityStart(project.getUtcStartTime())
			.validityEnd(project.getUtcEndTime())
			.leader(leader)
			.build();
		createOrUpdate(project.getId(), projectInstallation);
	}

	@Override
	@Transactional
	public void update(Project project) {
		Map<String, ProjectInstallationJob> siteIdToInstallationJob = projectOperationRepository.findProjectInstallation(project.getId()).stream()
			.collect(Collectors.toMap(x -> x.siteId, x -> x));
		Set<ProjectUpdateStatus> updateStatues = projectOperationRepository.findProjectUpdateStatues(project.getId());

		if(hasProjectNotTerminalStateInAnySite(updateStatues, siteIdToInstallationJob))
			throw new IllegalStateException("Project updating while project installing is not supported");

		siteRepository.findByProjectId(project.getId()).forEach(siteId -> {
			ProjectInstallationJob job = siteIdToInstallationJob.get(siteId.id);
			if(ProjectInstallationStatus.FAILED.equals(job.status)){
				update(siteId, project);
				return;
			}

			ProjectUpdateJob projectUpdateJob = ProjectUpdateJob.builder()
				.correlationId(CorrelationId.randomID())
				.siteId(siteId.id)
				.projectId(project.getId())
				.status(ProjectUpdateStatus.PENDING)
				.build();
			projectOperationRepository.createOrUpdate(projectUpdateJob);
			runAfterCommit(() -> siteAgentProjectOperationService.updateProject(
				projectUpdateJob.correlationId,
				siteId.externalId,
				project,
				usersDAO.findById(project.getLeaderId()).get()
			));
			LOG.info("ProjectUpdateJob was created: {}", projectUpdateJob);
		});
	}

	private boolean hasProjectNotTerminalStateInAnySite(Set<ProjectUpdateStatus> updateStatuses,
	                                                    Map<String, ProjectInstallationJob> siteIdToInstallStatues) {
		return 
			(!siteIdToInstallStatues.isEmpty() && siteIdToInstallStatues.values().stream().noneMatch(job -> job.status.isTerminal()))
			|| (!updateStatuses.isEmpty() && updateStatuses.stream().noneMatch(ProjectUpdateStatus::isTerminal));
	}

	@Override
	@Transactional
	public void remove(String projectId) {
		siteRepository.findByProjectId(projectId).forEach(siteId -> {
			CorrelationId correlationId = CorrelationId.randomID();
			siteAgentProjectOperationService.removeProject(
				correlationId,
				siteId.externalId,
				projectId
			);
			LOG.info("ProjectRemovalJob was created: {}", correlationId);
		});
	}
}
