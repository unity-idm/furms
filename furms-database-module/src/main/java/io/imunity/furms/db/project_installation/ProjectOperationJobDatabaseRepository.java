/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationId;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationJobStatus;
import io.imunity.furms.domain.project_installation.ProjectInstallationResult;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateId;
import io.imunity.furms.domain.project_installation.ProjectUpdateJob;
import io.imunity.furms.domain.project_installation.ProjectUpdateJobStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateResult;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Gid;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.sites.SiteInstalledProject;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Repository
class ProjectOperationJobDatabaseRepository implements ProjectOperationRepository {
	private final ProjectInstallationJobEntityRepository installationRepository;
	private final ProjectUpdateJobEntityRepository updateRepository;

	ProjectOperationJobDatabaseRepository(ProjectInstallationJobEntityRepository installationRepository,
	                                      ProjectUpdateJobEntityRepository updateRepository) {
		this.installationRepository = installationRepository;
		this.updateRepository = updateRepository;
	}

	@Override
	public Optional<ProjectInstallationJob> findInstallationJobByCorrelationId(CorrelationId correlationId) {
		return installationRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(job -> ProjectInstallationJob.builder()
				.id(job.getId().toString())
				.correlationId(new CorrelationId(job.correlationId.toString()))
				.siteId(job.siteId.toString())
				.projectId(job.projectId.toString())
				.status(ProjectInstallationStatus.valueOf(job.status))
				.gid(job.gid)
				.build());
	}

	@Override
	public Optional<ProjectUpdateJob> findUpdateJobByCorrelationId(CorrelationId correlationId) {
		return updateRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(job -> ProjectUpdateJob.builder()
				.id(job.getId().toString())
				.correlationId(new CorrelationId(job.correlationId.toString()))
				.siteId(job.siteId.toString())
				.projectId(job.projectId.toString())
				.status(ProjectUpdateStatus.valueOf(job.status))
				.build());
	}

	@Override
	public ProjectInstallation findProjectInstallation(ProjectAllocationId projectAllocationId,
	                                                   Function<PersistentId, Optional<FURMSUser>> userGetter) {
		ProjectInstallationEntity allocation = installationRepository
				.findByProjectAllocationId(projectAllocationId.id);
		final FURMSUser leader = allocation.leaderId != null
				? userGetter.apply(new PersistentId(allocation.leaderId)).orElse(null)
				: null;
		return ProjectInstallation.builder()
			.id(allocation.id)
			.siteId(allocation.siteId)
			.siteExternalId(allocation.siteExternalId)
			.name(allocation.name)
			.description(allocation.description)
			.communityId(allocation.communityId)
			.communityName(allocation.communityName)
			.acronym(allocation.acronym)
			.researchField(allocation.researchField)
			.validityStart(allocation.validityStart)
			.validityEnd(allocation.validityEnd)
			.leader(leader)
			.build();
	}

	@Override
	public String createOrUpdate(ProjectInstallationJob projectInstallationJob) {
		UUID id = installationRepository.findBySiteIdAndProjectId(
			projectInstallationJob.siteId.id, projectInstallationJob.projectId.id
			)
			.map(UUIDIdentifiable::getId)
			.orElse(null);
		ProjectInstallationJobEntity projectInstallationJobEntity = ProjectInstallationJobEntity.builder()
			.id(id)
			.correlationId(UUID.fromString(projectInstallationJob.correlationId.id))
			.siteId(projectInstallationJob.siteId.id)
			.projectId(projectInstallationJob.projectId.id)
			.status(projectInstallationJob.status)
			.build();
		ProjectInstallationJobEntity job = installationRepository.save(projectInstallationJobEntity);
		return job.getId().toString();
	}

	@Override
	public String createOrUpdate(ProjectUpdateJob projectUpdateJob) {
		UUID id = updateRepository.findByProjectIdAndSiteId(projectUpdateJob.projectId.id, projectUpdateJob.siteId.id)
			.map(UUIDIdentifiable::getId)
			.orElse(null);
		ProjectUpdateJobEntity projectUpdateJobEntity = ProjectUpdateJobEntity.builder()
			.id(id)
			.correlationId(UUID.fromString(projectUpdateJob.correlationId.id))
			.siteId(projectUpdateJob.siteId.id)
			.projectId(projectUpdateJob.projectId.id)
			.status(projectUpdateJob.status)
			.build();
		ProjectUpdateJobEntity job = updateRepository.save(projectUpdateJobEntity);
		return job.getId().toString();
	}

	@Override
	public void update(ProjectInstallationId id, ProjectInstallationResult result) {
		installationRepository.findById(id.id)
			.map(job -> ProjectInstallationJobEntity.builder()
				.id(job.getId())
				.correlationId(job.correlationId)
				.siteId(job.siteId)
				.projectId(job.projectId)
				.status(result.status)
				.gid(ofNullable(result.attributes).map(attributes -> attributes.get("gid")).orElse(null))
				.code(result.error.code)
				.message(result.error.message)
				.build())
			.ifPresent(installationRepository::save);
	}

	@Override
	public void update(ProjectUpdateId id, ProjectUpdateResult result) {
		updateRepository.findById(id.id)
			.map(job -> ProjectUpdateJobEntity.builder()
				.id(job.getId())
				.correlationId(job.correlationId)
				.siteId(job.siteId)
				.projectId(job.projectId)
				.status(result.status)
				.code(result.error.code)
				.message(result.error.message)
				.build())
			.ifPresent(updateRepository::save);
	}

	@Override
	public boolean installedProjectExistsBySiteIdAndProjectId(SiteId siteId, ProjectId projectId) {
		return installationRepository.existsBySiteIdAndProjectIdAndStatus(
			siteId.id,
			projectId.id,
			ProjectInstallationStatus.INSTALLED.getPersistentId()
		);
	}

	@Override
	public boolean areAllProjectOperationInTerminateState(ProjectId projectId) {
		return !(installationRepository.existsByProjectIdAndStatusOrProjectIdAndStatus(
			projectId.id,
			ProjectInstallationStatus.PENDING.getPersistentId(),
			projectId.id,
			ProjectInstallationStatus.ACKNOWLEDGED.getPersistentId()
		) ||
		updateRepository.existsByProjectIdAndStatusOrProjectIdAndStatus(
			projectId.id,
			ProjectUpdateStatus.PENDING.getPersistentId(),
			projectId.id,
			ProjectUpdateStatus.ACKNOWLEDGED.getPersistentId()
		));
	}

	@Override
	public Set<ProjectInstallationJobStatus> findAllBySiteId(SiteId siteId) {
		return installationRepository.findAllBySiteId(siteId.id).stream()
				.map(job -> ProjectInstallationJobStatus.builder()
						.siteId(job.siteId.toString())
						.siteName(job.siteName)
						.projectId(job.projectId.toString())
						.status(ProjectInstallationStatus.valueOf(job.status))
						.errorMessage(job.code, job.message)
						.build())
				.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectInstallationJobStatus> findAllByCommunityId(CommunityId communityId) {
		return installationRepository.findAllByCommunityId(communityId.id).stream()
			.map(job -> ProjectInstallationJobStatus.builder()
				.siteId(job.siteId.toString())
				.siteName(job.siteName)
				.projectId(job.projectId.toString())
				.status(ProjectInstallationStatus.valueOf(job.status))
				.errorMessage(job.code, job.message)
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectUpdateJobStatus> findAllUpdatesByCommunityId(CommunityId communityId) {
		return updateRepository.findAllByCommunityId(communityId.id).stream()
			.map(job -> ProjectUpdateJobStatus.builder()
				.siteId(job.siteId.toString())
				.projectId(job.projectId.toString())
				.status(ProjectUpdateStatus.valueOf(job.status))
				.errorMessage(job.code, job.message)
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectInstallationJobStatus> findAllByProjectId(ProjectId projectId) {
		return installationRepository.findAllByProjectId(projectId.id).stream()
			.map(job -> ProjectInstallationJobStatus.builder()
				.siteId(job.siteId.toString())
				.siteName(job.siteName)
				.projectId(job.projectId.toString())
				.status(ProjectInstallationStatus.valueOf(job.status))
				.errorMessage(job.code, job.message)
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectUpdateJobStatus> findAllUpdatesByProjectId(ProjectId projectId) {
		return updateRepository.findByProjectId(projectId.id).stream()
			.map(job -> ProjectUpdateJobStatus.builder()
				.projectId(job.projectId.toString())
				.siteId(job.siteId.toString())
				.status(ProjectUpdateStatus.valueOf(job.status))
				.errorMessage(job.code, job.message)
				.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectInstallationJob> findProjectInstallation(ProjectId projectId) {
		return installationRepository.findByProjectId(projectId.id).stream()
			.map(installation -> ProjectInstallationJob.builder()
				.id(installation.getId().toString())
				.projectId(installation.projectId.toString())
				.siteId(installation.siteId.toString())
				.correlationId(new CorrelationId(installation.correlationId.toString()))
				.status(ProjectInstallationStatus.valueOf(installation.status))
				.gid(installation.gid)
				.build()
			).collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectUpdateStatus> findProjectUpdateStatues(ProjectId projectId) {
		return updateRepository.findByProjectId(projectId.id).stream()
			.map(x -> ProjectUpdateStatus.valueOf(x.status))
			.collect(Collectors.toSet());
	}

	@Override
	public Set<SiteInstalledProject> findAllSiteInstalledProjectsBySiteId(SiteId siteId) {
		return installationRepository.findAllInstalledBySiteId(siteId.id).stream()
				.map(this::convertToSiteInstalledProject)
				.collect(Collectors.toSet());
	}

	@Override
	public Set<SiteInstalledProject> findAllSiteInstalledProjectsByProjectId(ProjectId projectId) {
		return installationRepository.findAllByProjectId(projectId.id).stream()
				.map(this::convertToSiteInstalledProject)
				.collect(Collectors.toSet());
	}

	@Override
	public void deleteById(ProjectInstallationId id) {
		installationRepository.deleteById(id.id);
	}

	@Override
	public void deleteAll() {
		installationRepository.deleteAll();
		updateRepository.deleteAll();
	}

	private SiteInstalledProject convertToSiteInstalledProject(ProjectInstallationJobStatusEntity installation) {
		return SiteInstalledProject.builder()
				.siteId(installation.siteId.toString())
				.siteName(installation.siteName)
				.projectId(installation.projectId.toString())
				.gid(new Gid(installation.gid))
				.build();
	}

	@Override
	public void delete(CorrelationId id) {
		installationRepository.deleteByCorrelationId(UUID.fromString(id.id));
		updateRepository.deleteByCorrelationId(UUID.fromString(id.id));
	}
}

