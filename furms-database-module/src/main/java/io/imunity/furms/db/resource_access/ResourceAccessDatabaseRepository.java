/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;


import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.GrantId;
import io.imunity.furms.domain.resource_access.ProjectUserGrant;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
class ResourceAccessDatabaseRepository implements ResourceAccessRepository {

	private final UserGrantEntityRepository userGrantEntityRepository;
	private final UserGrantJobEntityRepository userGrantJobEntityRepository;

	ResourceAccessDatabaseRepository(UserGrantEntityRepository userGrantEntityRepository, UserGrantJobEntityRepository userGrantJobEntityRepository) {
		this.userGrantEntityRepository = userGrantEntityRepository;
		this.userGrantJobEntityRepository = userGrantJobEntityRepository;
	}

	@Override
	public Set<UserGrant> findUsersGrantsByProjectId(ProjectId projectId) {
		return userGrantEntityRepository.findAll(projectId.id).stream()
			.map(this::map)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<FenixUserId> findUsersBySiteId(SiteId siteId) {
		return userGrantEntityRepository.findAllBySiteId(siteId.id).stream()
			.map(grant -> new FenixUserId(grant.userId))
			.collect(Collectors.toSet());
	}

	@Override
	public Optional<ProjectUserGrant> findUsersGrantsByCorrelationId(CorrelationId correlationId) {
		return userGrantEntityRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(entity -> new ProjectUserGrant(
				new SiteId(entity.siteId),
				new GrantId(entity.grantId),
				new ProjectId(entity.projectId),
				new FenixUserId(entity.userId))
			);
	}

	@Override
	public Set<UserGrant> findUserGrantsByProjectIdAndFenixUserId(ProjectId projectId, FenixUserId fenixUserId) {
		return userGrantEntityRepository.findAll(projectId.id, fenixUserId.id).stream()
			.map(this::map)
			.collect(Collectors.toSet());
	}

	@Override
	public boolean existsBySiteIdAndProjectIdAndFenixUserId(SiteId siteId, ProjectId projectId, FenixUserId fenixUserId) {
		return userGrantEntityRepository.existsBySiteIdAndProjectIdAndUserId(siteId.id, projectId.id,
			fenixUserId.id);
	}

	private UserGrant map(UserGrantResolved userAllocationResolved) {
		return UserGrant.builder()
			.projectAllocationId(userAllocationResolved.allocation.projectAllocationId.toString())
			.userId(userAllocationResolved.allocation.userId)
			.status(AccessStatus.valueOf(userAllocationResolved.job.status))
			.message(userAllocationResolved.job.message)
			.build();
	}

	@Override
	public UUID create(CorrelationId correlationId, GrantAccess grantAccess, AccessStatus accessStatus) {
		UserGrantEntity save = userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(grantAccess.siteId.id)
				.projectId(grantAccess.projectId.id)
				.projectAllocationId(grantAccess.allocationId.id)
				.userId(grantAccess.fenixUserId.id)
				.build()
		);
		userGrantJobEntityRepository.save(UserGrantJobEntity.builder()
			.correlationId(UUID.fromString(correlationId.id))
			.userAllocationId(save.getId())
			.status(accessStatus)
			.build()
		);
		return save.getId();
	}

	public boolean exists(GrantAccess grantAccess) {
		return userGrantEntityRepository.findByUserIdAndProjectAllocationId(grantAccess.fenixUserId.id,
				grantAccess.allocationId.id)
			.isPresent();
	}

	@Override
	public AccessStatus findCurrentStatus(FenixUserId userId, ProjectAllocationId allocationId) {
		return userGrantEntityRepository
			.findByUserIdAndProjectAllocationId(userId.id, allocationId.id)
			.map(userGrantResolved -> userGrantResolved.job.status)
			.map(AccessStatus::valueOf)
			.orElseThrow(() -> new IllegalArgumentException(String.format("UserGant with user id %s and allocation id %s doesn't exist", userId, allocationId)));
	}

	@Override
	public Set<GrantAccess> findWaitingGrantAccesses(FenixUserId userId, ProjectId projectId, SiteId siteId) {
		return userGrantEntityRepository.findByUserIdAndProjectIdAndSiteId(userId.id, projectId.id,
				siteId.id, AccessStatus.USER_INSTALLING.getPersistentId()).stream()
			.map(x -> GrantAccess.builder()
				.siteId(new SiteId(x.siteId.toString(), x.siteExternalId))
				.fenixUserId(new FenixUserId(x.userId))
				.projectId(x.projectId.toString())
				.allocationId(x.projectAllocationId.toString())
				.build())
			.collect(Collectors.toSet());
	}

	@Override
	public Set<GrantAccess> findGrantAccessesBy(SiteId siteId, ProjectAllocationId projectAllocationId) {
		return userGrantEntityRepository.findBySiteIdAndProjectAllocationId(siteId.id,
				projectAllocationId.id).stream()
			.map(x -> GrantAccess.builder()
				.siteId(new SiteId(x.siteId.toString(), x.siteExternalId))
				.fenixUserId(new FenixUserId(x.userId))
				.projectId(x.projectId.toString())
				.allocationId(x.projectAllocationId.toString())
				.build())
			.collect(Collectors.toSet());
	}

	@Override
	public Optional<AccessStatus> findCurrentStatus(CorrelationId correlationId) {
		return userGrantJobEntityRepository
			.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(userGrantJob -> userGrantJob.status)
			.map(AccessStatus::valueOf);
	}

	@Override
	public void update(CorrelationId correlationId, GrantAccess grantAccess, AccessStatus status) {
		UserGrantResolved userAllocation = userGrantEntityRepository
			.findByUserIdAndProjectAllocationId(grantAccess.fenixUserId.id, grantAccess.allocationId.id)
			.orElseThrow(() -> new IllegalArgumentException("GrantAccess not found: " + grantAccess));

		userGrantJobEntityRepository.save(UserGrantJobEntity.builder()
			.id(userAllocation.job.getId())
			.correlationId(UUID.fromString(correlationId.id))
			.userAllocationId(userAllocation.job.userGrantId)
			.status(status)
			.build()
		);
	}

	@Override
	public void update(CorrelationId correlationId, AccessStatus status, String msg) {
		userGrantJobEntityRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(old ->
					UserGrantJobEntity.builder()
						.id(old.getId())
						.userAllocationId(old.userGrantId)
						.correlationId(old.correlationId)
						.status(status)
						.message(msg)
						.build()
				)
			.map(userGrantJobEntityRepository::save)
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
	}

	@Override
	public void deleteByCorrelationId(CorrelationId correlationId){
		UserGrantJobEntity userGrantJobEntity = userGrantJobEntityRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
		userGrantEntityRepository.deleteById(userGrantJobEntity.userGrantId);
	}

	@Override
	public void deleteByUserAndAllocationId(FenixUserId userId, String allocationId){
		userGrantEntityRepository.findByUserIdAndProjectAllocationId(userId.id, UUID.fromString(allocationId))
			.ifPresent(x -> userGrantEntityRepository.deleteById(x.allocation.getId()));
	}

	@Override
	public void deleteByUserAndProjectId(FenixUserId userId, ProjectId projectId) {
		Set<UserGrantEntity> userGrants = userGrantEntityRepository.findByUserIdAndProjectId(userId.id, projectId.id);
		userGrantEntityRepository.deleteAll(userGrants);
	}

	@Override
	public void deleteByUserAndSiteIdAndProjectId(FenixUserId userId, SiteId siteId, ProjectId projectId) {
		Set<UserGrantEntity> userGrants = userGrantEntityRepository.findByUserIdAndSiteIdAndProjectId(userId.id,
			siteId.id, projectId.id);
		userGrantEntityRepository.deleteAll(userGrants);
	}

	@Override
	public void deleteAll() {
		userGrantEntityRepository.deleteAll();
	}
}
