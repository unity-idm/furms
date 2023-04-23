/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionErrorMessage;
import io.imunity.furms.domain.user_operation.UserAdditionId;
import io.imunity.furms.domain.user_operation.UserAdditionJob;
import io.imunity.furms.domain.user_operation.UserAdditionWithProject;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toSet;

@Repository
class UserOperationDatabaseRepository implements UserOperationRepository {

	private final UserAdditionEntityRepository userAdditionEntityRepository;
	private final UserAdditionJobEntityRepository userAdditionJobEntityRepository;

	UserOperationDatabaseRepository(UserAdditionEntityRepository userAdditionEntityRepository, UserAdditionJobEntityRepository userAdditionJobEntityRepository) {
		this.userAdditionEntityRepository = userAdditionEntityRepository;
		this.userAdditionJobEntityRepository = userAdditionJobEntityRepository;
	}

	@Override
	public Set<String> findUserIds(ProjectId projectId) {
		return userAdditionEntityRepository.findAllByProjectId(projectId.id).stream()
				.map(x -> x.userId)
				.collect(toSet());
	}

	@Override
	public Set<UserAddition> findAllUserAdditions(ProjectId projectId) {
		return userAdditionEntityRepository.findAllExtendedByProjectId(projectId.id).stream()
			.map(UserAdditionReadEntity::toUserAddition)
			.collect(toSet());
	}

	@Override
	public Set<UserAddition> findAllUserAdditions(ProjectId projectId, FenixUserId userId) {
		return userAdditionEntityRepository.findAllByProjectIdAndUserId(projectId.id, userId.id).stream()
			.map(UserAdditionReadEntity::toUserAddition)
			.collect(toSet());
	}

	@Override
	public Set<UserAddition> findAllUserAdditions(SiteId siteId, ProjectId projectId) {
		return userAdditionEntityRepository.findAllBySiteIdAndProjectId(siteId.id, projectId.id).stream()
			.map(UserAdditionReadEntity::toUserAddition)
			.collect(toSet());
	}

	@Override
	public Optional<UserAddition> findUserAddition(SiteId siteId, ProjectId projectId, FenixUserId userId) {
		return userAdditionEntityRepository.findBySiteIdAndProjectIdAndUserId(siteId.id, projectId.id, userId.id)
			.map(UserAdditionReadEntity::toUserAddition);
	}

	@Override
	public Set<UserAddition> findAllUserAdditions(FenixUserId userId) {
		return userAdditionEntityRepository.findAllByUserId(userId.id).stream()
			.map(UserAdditionReadEntity::toUserAddition)
			.collect(toSet());
	}

	@Override
	public Set<UserAddition> findAllUserAdditions(SiteId siteId, FenixUserId userId) {
		return userAdditionEntityRepository.findAllBySiteIdAndUserId(siteId.id, userId.id).stream()
			.map(UserAdditionReadEntity::toUserAddition)
			.collect(toSet());
	}

	@Override
	public Set<UserAddition> findAllUserAdditionsBySiteId(SiteId siteId) {
		return userAdditionEntityRepository.findAllBySiteId(siteId.id).stream()
				.map(UserAdditionReadEntity::toUserAddition)
				.collect(toSet());
	}

	@Override
	public Set<UserAddition> findAllUserAdditionsByProjectId(ProjectId projectId) {
		return userAdditionEntityRepository.findExtendedAllByProjectId(projectId.id).stream()
			.map(UserAdditionReadEntity::toUserAddition)
			.collect(toSet());
	}

	@Override
	public Set<UserAdditionWithProject> findAllUserAdditionsWithSiteAndProjectBySiteId(FenixUserId userId, SiteId siteId) {
		return userAdditionEntityRepository.findAllWithSiteAndProjectsBySiteIdAndUserId(siteId.id, userId.id).stream()
				.map(userAddition -> UserAdditionWithProject.builder()
						.siteName(userAddition.siteName)
						.projectId(userAddition.projectId)
						.projectName(userAddition.projectName)
						.localProjectName(userAddition.gid)
						.userId(userAddition.uid)
						.status(UserStatus.valueOf(userAddition.status))
						.errorMessage(new UserAdditionErrorMessage(userAddition.code, userAddition.message))
						.build())
				.collect(toSet());
	}

	@Override
	public UserAdditionId create(UserAddition userAddition) {
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(userAddition.siteId.id)
				.projectId(userAddition.projectId.id)
				.userId(userAddition.userId.id)
				.build()
		);
		userAdditionJobEntityRepository.save(
			UserAdditionJobEntity.builder()
				.correlationId(fromString(userAddition.correlationId.id))
				.userAdditionId(userAdditionSaveEntity.getId())
				.status(userAddition.status)
				.build()
		);
		return new UserAdditionId(userAdditionSaveEntity.getId());
	}

	@Override
	public void update(UserAddition userAddition) {
		userAdditionEntityRepository.findByCorrelationId(UUID.fromString(userAddition.correlationId.id))
			.or(() -> userAdditionEntityRepository.findById(userAddition.id.id))
			.map(old -> UserAdditionSaveEntity.builder()
				.id(old.getId())
				.siteId(old.siteId)
				.projectId(old.projectId)
				.userId(old.userId)
				.uid(userAddition.uid)
				.build()
			).ifPresent(userAdditionEntityRepository::save);
		userAdditionJobEntityRepository.findByCorrelationId(UUID.fromString(userAddition.correlationId.id))
			.or(() -> userAdditionJobEntityRepository.findByUserAdditionId(userAddition.id.id))
			.map(old -> UserAdditionJobEntity.builder()
				.id(old.getId())
				.correlationId(fromString(userAddition.correlationId.id))
				.userAdditionId(old.userAdditionId)
				.status(userAddition.status)
				.code(userAddition.errorMessage.map(e -> e.code).orElse(null))
				.message(userAddition.errorMessage.map(e -> e.message).orElse(null))
				.build()
			).ifPresent(userAdditionJobEntityRepository::save);
	}

	@Override
	public Optional<UserStatus> findAdditionStatusByCorrelationId(CorrelationId correlationId) {
		return userAdditionJobEntityRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(x -> UserStatus.valueOf(x.status));
	}

	@Override
	public Optional<UserStatus> findAdditionStatus(SiteId siteId, ProjectId projectId, FenixUserId userId) {
		return userAdditionEntityRepository.findStatusBySiteIdAndProjectIdAndUserId(siteId.id, projectId.id, userId.id)
			.map(UserStatus::valueOf);
	}

	@Override
	public UserAddition findAdditionByCorrelationId(CorrelationId correlationId) {
		return userAdditionEntityRepository.findReadEntityByCorrelationId(UUID.fromString(correlationId.id))
			.map(userAddition -> UserAddition.builder()
				.siteId(new SiteId(userAddition.site.getId().toString(), userAddition.site.getExternalId()))
				.userId(userAddition.userId)
				.projectId(userAddition.projectId.toString())
				.correlationId(new CorrelationId(userAddition.correlationId.toString()))
				.build())
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
	}

	@Override
	public void deleteByCorrelationId(CorrelationId correlationId) {
		userAdditionJobEntityRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.ifPresent(x -> userAdditionEntityRepository.deleteById(x.userAdditionId));
	}

	@Override
	public boolean existsByUserIdAndSiteIdAndProjectId(FenixUserId userId, SiteId siteId, ProjectId projectId) {
		return userAdditionEntityRepository.existsBySiteIdAndProjectIdAndUserId(siteId.id, projectId.id,
			userId.id);
	}

	@Override
	public boolean isUserInstalledOnSite(FenixUserId userId, SiteId siteId){
		return userAdditionEntityRepository.findStatusBySiteIdAndUserId(siteId.id, userId.id).stream()
			.map(UserStatus::valueOf)
			.anyMatch(UserStatus::isInstalled);
	}


	@Override
	public void deleteAll() {
		userAdditionEntityRepository.deleteAll();
	}

	@Override
	public void delete(UserAddition userAddition) {
		userAdditionEntityRepository.deleteById(userAddition.id.id);
	}

	@Override
	public void update(UserAdditionJob userAdditionJob) {
		userAdditionJobEntityRepository.findByUserAdditionId(userAdditionJob.userAdditionId.id)
			.map(x -> UserAdditionJobEntity.builder()
				.id(x.getId())
				.correlationId(fromString(userAdditionJob.correlationId.id))
				.userAdditionId(userAdditionJob.userAdditionId.id)
				.status(userAdditionJob.status)
				.code(userAdditionJob.errorMessage.map(e -> e.code).orElse(null))
				.message(userAdditionJob.errorMessage.map(e -> e.message).orElse(null))
				.build())
			.ifPresent(userAdditionJobEntityRepository::save);
	}

	@Override
	public void updateStatus(CorrelationId correlationId, UserStatus userStatus, Optional<UserAdditionErrorMessage> userErrorMessage) {
		userAdditionJobEntityRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(oldEntity -> UserAdditionJobEntity.builder()
				.id(oldEntity.getId())
				.correlationId(oldEntity.correlationId)
				.userAdditionId(oldEntity.userAdditionId)
				.status(userStatus)
				.code(userErrorMessage.map(e -> e.code).orElse(null))
				.message(userErrorMessage.map(e -> e.message).orElse(null))
				.build())
			.ifPresent(userAdditionJobEntityRepository::save);
	}

	@Override
	public boolean isUserAdded(SiteId siteId, FenixUserId userId) {
		return userAdditionEntityRepository.existsBySiteIdAndUserId(siteId.id, userId.id);
	}
}
