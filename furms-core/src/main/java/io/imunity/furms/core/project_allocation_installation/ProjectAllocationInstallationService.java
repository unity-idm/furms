/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.core.post_commit.PostCommitRunner;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.PROJECT_INSTALLATION_FAILED;

@Service
public class ProjectAllocationInstallationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;
	private final ProjectAllocationRepository projectAllocationRepository;
	private final SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService;
	private final PostCommitRunner postCommitRunner;

	ProjectAllocationInstallationService(ProjectAllocationInstallationRepository projectAllocationInstallationRepository,
	                                     ProjectAllocationRepository projectAllocationRepository,
	                                     SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService,
	                                     PostCommitRunner postCommitRunner) {
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
		this.projectAllocationRepository = projectAllocationRepository;
		this.siteAgentProjectAllocationInstallationService = siteAgentProjectAllocationInstallationService;
		this.postCommitRunner = postCommitRunner;
	}

	public Set<ProjectAllocationInstallation> findAll(ProjectId projectId) {
		return projectAllocationInstallationRepository.findAll(projectId);
	}

	public Set<ProjectDeallocation> findAllUninstallation(ProjectId projectId) {
		return projectAllocationInstallationRepository.findAllDeallocation(projectId);
	}

	public Set<ProjectAllocationChunk> findAllChunks(ProjectId projectId) {
		return projectAllocationInstallationRepository.findAllChunks(projectId);
	}

	public Set<ProjectAllocationChunk> findAllChunksByAllocationId(ProjectAllocationId projectAllocationId) {
		return projectAllocationInstallationRepository.findAllChunksByAllocationId(projectAllocationId);
	}

	@Transactional
	public void createAllocation(ProjectAllocationId projectAllocationId) {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId).get();
		ProjectAllocationInstallation projectAllocationInstallation = ProjectAllocationInstallation.builder()
			.correlationId(correlationId)
			.siteId(projectAllocationResolved.site.getId())
			.projectAllocationId(projectAllocationId)
			.status(ProjectAllocationInstallationStatus.PROVISIONING_PROJECT)
			.build();

		projectAllocationInstallationRepository.create(projectAllocationInstallation);
		LOG.info("ProjectAllocationInstallation was updated: {}", projectAllocationInstallation);
	}

	@Transactional
	public void updateAndStartAllocation(ProjectAllocationId projectAllocationId) {
		CorrelationId correlationId = CorrelationId.randomID();
		projectAllocationInstallationRepository.update(projectAllocationId, ProjectAllocationInstallationStatus.UPDATING, correlationId);
		ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId).get();
		postCommitRunner.runAfterCommit(() ->
			siteAgentProjectAllocationInstallationService.allocateProject(correlationId, projectAllocationResolved)
		);

		LOG.info("ProjectAllocationInstallation with project allocation {} was update to status UPDATING", projectAllocationId);
	}

	@Transactional
	public void createAndStartAllocation(ProjectAllocationId projectAllocationId) {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId).get();
		ProjectAllocationInstallation projectAllocationInstallation = ProjectAllocationInstallation.builder()
			.correlationId(correlationId)
			.siteId(projectAllocationResolved.site.getId())
			.projectAllocationId(projectAllocationId)
			.status(ProjectAllocationInstallationStatus.INSTALLING)
			.build();
		projectAllocationInstallationRepository.create(projectAllocationInstallation);
		postCommitRunner.runAfterCommit(
			() -> siteAgentProjectAllocationInstallationService.allocateProject(correlationId, projectAllocationResolved)
		);
		LOG.info("ProjectAllocationInstallation was updated: {}", projectAllocationInstallation);
	}

	@Transactional
	public void startWaitingAllocations(ProjectId projectId, SiteId siteId) {
		projectAllocationInstallationRepository.findAll(projectId, siteId).forEach(allocation -> {
			projectAllocationInstallationRepository.update(allocation.correlationId, ProjectAllocationInstallationStatus.INSTALLING, Optional.empty());
			ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(allocation.projectAllocationId)
				.orElseThrow(() -> new IllegalArgumentException("Project Allocation Id doesn't exist"));
			postCommitRunner.runAfterCommit(() ->
				siteAgentProjectAllocationInstallationService.allocateProject(allocation.correlationId,
					projectAllocationResolved)
			);
			LOG.info("ProjectAllocationInstallation with given correlationId {} was updated to: {}", allocation.correlationId.id, ProjectAllocationInstallationStatus.INSTALLING);
		});
	}

	@Transactional
	public void cancelWaitingAllocations(ProjectId projectId, ErrorMessage errorMessage) {
		projectAllocationInstallationRepository.findAll(projectId).forEach(allocation -> {
			projectAllocationInstallationRepository.update(
				allocation.correlationId,
				PROJECT_INSTALLATION_FAILED,
				Optional.of(errorMessage)
			);
			LOG.info("ProjectAllocationInstallation with given correlationId {} was updated to: {}", allocation.correlationId.id, PROJECT_INSTALLATION_FAILED);
		});
	}

	@Transactional
	public void createDeallocation(ProjectAllocationResolved projectAllocationResolved) {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectDeallocation projectDeallocation = ProjectDeallocation.builder()
			.siteId(projectAllocationResolved.site.getId())
			.correlationId(correlationId)
			.projectAllocationId(projectAllocationResolved.id)
			.status(ProjectDeallocationStatus.PENDING)
			.build();
		ProjectAllocationInstallation projectAllocationInstallation =
			projectAllocationInstallationRepository.findByProjectAllocationId(projectDeallocation.projectAllocationId);
		if(projectAllocationInstallation.status.isFailed()){
			projectAllocationRepository.deleteById(projectAllocationInstallation.projectAllocationId);
			LOG.info("Deallocation was not created, because project allocation {} process failed", projectAllocationInstallation.id);
			return;
		}
		projectAllocationInstallationRepository.create(projectDeallocation);
		postCommitRunner.runAfterCommit(() ->
			siteAgentProjectAllocationInstallationService.deallocateProject(correlationId, projectAllocationResolved)
		);
		LOG.info("ProjectDeallocation was created: {}", projectDeallocation);
	}
}
