/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationJobStatus;
import io.imunity.furms.domain.project_installation.ProjectInstallationResult;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateJob;
import io.imunity.furms.domain.project_installation.ProjectUpdateJobStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateResult;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Gid;
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
	public ProjectInstallationJob findInstallationJobByCorrelationId(CorrelationId correlationId) {
		ProjectInstallationJobEntity job = installationRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
		return ProjectInstallationJob.builder()
			.id(job.getId().toString())
			.correlationId(new CorrelationId(job.correlationId.toString()))
			.siteId(job.siteId.toString())
			.projectId(job.projectId.toString())
			.status(ProjectInstallationStatus.valueOf(job.status))
			.gid(job.gid)
			.build();
	}

	@Override
	public ProjectUpdateJob findUpdateJobByCorrelationId(CorrelationId correlationId) {
		ProjectUpdateJobEntity job = updateRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
		return ProjectUpdateJob.builder()
			.id(job.getId().toString())
			.correlationId(new CorrelationId(job.correlationId.toString()))
			.siteId(job.siteId.toString())
			.projectId(job.projectId.toString())
			.status(ProjectUpdateStatus.valueOf(job.status))
			.build();
	}

	@Override
	public ProjectInstallation findProjectInstallation(String projectAllocationId,
			Function<PersistentId, Optional<FURMSUser>> userGetter) {
		ProjectInstallationEntity allocation = installationRepository
				.findByProjectAllocationId(UUID.fromString(projectAllocationId));
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
		UUID id = installationRepository.findBySiteIdAndProjectId(UUID.fromString(projectInstallationJob.siteId), UUID.fromString(projectInstallationJob.projectId))
			.map(UUIDIdentifiable::getId)
			.orElse(null);
		ProjectInstallationJobEntity projectInstallationJobEntity = ProjectInstallationJobEntity.builder()
			.id(id)
			.correlationId(UUID.fromString(projectInstallationJob.correlationId.id))
			.siteId(UUID.fromString(projectInstallationJob.siteId))
			.projectId(UUID.fromString(projectInstallationJob.projectId))
			.status(projectInstallationJob.status)
			.build();
		ProjectInstallationJobEntity job = installationRepository.save(projectInstallationJobEntity);
		return job.getId().toString();
	}

	@Override
	public String createOrUpdate(ProjectUpdateJob projectUpdateJob) {
		UUID id = updateRepository.findByProjectIdAndSiteId(UUID.fromString(projectUpdateJob.projectId), UUID.fromString(projectUpdateJob.siteId))
			.map(UUIDIdentifiable::getId)
			.orElse(null);
		ProjectUpdateJobEntity projectUpdateJobEntity = ProjectUpdateJobEntity.builder()
			.id(id)
			.correlationId(UUID.fromString(projectUpdateJob.correlationId.id))
			.siteId(UUID.fromString(projectUpdateJob.siteId))
			.projectId(UUID.fromString(projectUpdateJob.projectId))
			.status(projectUpdateJob.status)
			.build();
		ProjectUpdateJobEntity job = updateRepository.save(projectUpdateJobEntity);
		return job.getId().toString();
	}

	@Override
	public String update(String id, ProjectInstallationResult result) {
		installationRepository.findById(UUID.fromString(id))
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
		return id;
	}

	@Override
	public String update(String id, ProjectUpdateResult result) {
		updateRepository.findById(UUID.fromString(id))
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
		return id;
	}

	@Override
	public boolean installedProjectExistsBySiteIdAndProjectId(String siteId, String projectId) {
		return installationRepository.existsBySiteIdAndProjectIdAndStatus(
			UUID.fromString(siteId),
			UUID.fromString(projectId),
			ProjectInstallationStatus.INSTALLED.getPersistentId()
		);
	}

	@Override
	public boolean areAllProjectOperationInTerminateState(String projectId) {
		return !(installationRepository.existsByProjectIdAndStatusOrProjectIdAndStatus(
			UUID.fromString(projectId),
			ProjectInstallationStatus.PENDING.getPersistentId(),
			UUID.fromString(projectId),
			ProjectInstallationStatus.ACKNOWLEDGED.getPersistentId()
		) ||
		updateRepository.existsByProjectIdAndStatusOrProjectIdAndStatus(
			UUID.fromString(projectId),
			ProjectUpdateStatus.PENDING.getPersistentId(),
			UUID.fromString(projectId),
			ProjectUpdateStatus.ACKNOWLEDGED.getPersistentId()
		));
	}

	@Override
	public Set<ProjectInstallationJobStatus> findAllBySiteId(String siteId) {
		return installationRepository.findAllBySiteId(UUID.fromString(siteId)).stream()
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
	public Set<ProjectInstallationJobStatus> findAllByCommunityId(String communityId) {
		return installationRepository.findAllByCommunityId(UUID.fromString(communityId)).stream()
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
	public Set<ProjectUpdateJobStatus> findAllUpdatesByCommunityId(String communityId) {
		return updateRepository.findAllByCommunityId(UUID.fromString(communityId)).stream()
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
	public Set<ProjectInstallationJobStatus> findAllByProjectId(String projectId) {
		return installationRepository.findAllByProjectId(UUID.fromString(projectId)).stream()
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
	public Set<ProjectUpdateJobStatus> findAllUpdatesByProjectId(String projectId) {
		return updateRepository.findByProjectId(UUID.fromString(projectId)).stream()
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
	public Set<ProjectInstallationJob> findProjectInstallation(String projectId) {
		return installationRepository.findByProjectId(UUID.fromString(projectId)).stream()
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
	public Set<ProjectUpdateStatus> findProjectUpdateStatues(String projectId) {
		return updateRepository.findByProjectId(UUID.fromString(projectId)).stream()
			.map(x -> ProjectUpdateStatus.valueOf(x.status))
			.collect(Collectors.toSet());
	}

	@Override
	public Set<SiteInstalledProject> findAllSiteInstalledProjectsBySiteId(String siteId) {
		return installationRepository.findAllInstalledBySiteId(UUID.fromString(siteId)).stream()
				.map(this::convertToSiteInstalledProject)
				.collect(Collectors.toSet());
	}

	@Override
	public Set<SiteInstalledProject> findAllSiteInstalledProjectsByProjectId(String projectId) {
		return installationRepository.findAllByProjectId(UUID.fromString(projectId)).stream()
				.map(this::convertToSiteInstalledProject)
				.collect(Collectors.toSet());
	}

	@Override
	public void deleteById(String id) {
		installationRepository.deleteById(UUID.fromString(id));
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

