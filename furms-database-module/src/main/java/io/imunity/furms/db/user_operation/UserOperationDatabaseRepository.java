/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionErrorMessage;
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
	public Set<String> findUserIds(String projectId) {
		return userAdditionEntityRepository.findAllByProjectId(UUID.fromString(projectId)).stream()
				.map(x -> x.userId)
				.collect(toSet());
	}

	@Override
	public Set<UserAddition> findAllUserAdditions(String projectId, String userId) {
		return userAdditionEntityRepository.findAllByProjectIdAndUserId(UUID.fromString(projectId), userId).stream()
			.map(UserAdditionReadEntity::toUserAddition)
			.collect(toSet());
	}
	
	@Override
	public Set<UserAddition> findAllUserAdditions(String userId) {
		return userAdditionEntityRepository.findAllByUserId(userId).stream()
			.map(UserAdditionReadEntity::toUserAddition)
			.collect(toSet());
	}

	@Override
	public Set<UserAddition> findAllUserAdditionsByUserId(String siteId) {
		return userAdditionEntityRepository.findAllBySiteId(UUID.fromString(siteId)).stream()
				.map(UserAdditionReadEntity::toUserAddition)
				.collect(toSet());
	}

	@Override
	public Set<UserAdditionWithProject> findAllUserAdditionsWithSiteAndProjectBySiteId(String userId, String siteId) {
		return userAdditionEntityRepository.findAllWithSiteAndProjectsBySiteIdAndUserId(UUID.fromString(siteId), userId).stream()
				.map(userAddition -> UserAdditionWithProject.builder()
						.siteName(userAddition.siteName)
						.projectId(userAddition.projectId)
						.projectName(userAddition.projectName)
						.userId(userAddition.uid)
						.status(UserStatus.valueOf(userAddition.status))
						.errorMessage(new UserAdditionErrorMessage(userAddition.code, userAddition.message))
						.build())
				.collect(toSet());
	}

	@Override
	public String create(UserAddition userAddition) {
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(fromString(userAddition.siteId.id))
				.projectId(fromString(userAddition.projectId))
				.userId(userAddition.userId)
				.build()
		);
		userAdditionJobEntityRepository.save(
			UserAdditionJobEntity.builder()
				.correlationId(fromString(userAddition.correlationId.id))
				.userAdditionId(userAdditionSaveEntity.getId())
				.status(userAddition.status)
				.build()
		);
		return userAdditionSaveEntity.getId().toString();
	}

	@Override
	public void update(UserAddition userAddition) {
		userAdditionEntityRepository.findByCorrelationId(UUID.fromString(userAddition.correlationId.id))
			.or(() -> userAdditionEntityRepository.findById(UUID.fromString(userAddition.id)))
			.map(old -> UserAdditionSaveEntity.builder()
				.id(old.getId())
				.siteId(old.siteId)
				.projectId(old.projectId)
				.userId(old.userId)
				.uid(userAddition.uid)
				.build()
			).ifPresent(userAdditionEntityRepository::save);
		userAdditionJobEntityRepository.findByCorrelationId(UUID.fromString(userAddition.correlationId.id))
			.or(() -> userAdditionJobEntityRepository.findByUserAdditionId(UUID.fromString(userAddition.id)))
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
	public UserStatus findAdditionStatusByCorrelationId(String correlationId) {
		return userAdditionJobEntityRepository.findByCorrelationId(UUID.fromString(correlationId))
			.map(x -> UserStatus.valueOf(x.status))
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
	}

	@Override
	public Optional<UserStatus> findAdditionStatus(String siteId, String projectId, FenixUserId userId) {
		return userAdditionEntityRepository.findStatusBySiteIdAndProjectIdAndUserId(UUID.fromString(siteId), UUID.fromString(projectId), userId.id)
			.map(UserStatus::valueOf);
	}

	@Override
	public UserAddition findAdditionByCorrelationId(CorrelationId correlationId) {
		return userAdditionEntityRepository.findReadEntityByCorrelationId(UUID.fromString(correlationId.id))
			.map(userAddition -> UserAddition.builder()
				.siteId(new SiteId(userAddition.site.getId().toString(), userAddition.site.getExternalId()))
				.userId(userAddition.userId)
				.projectId(userAddition.projectId.toString())
				.build())
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
	}

	@Override
	public String findSiteIdByCorrelationId(CorrelationId correlationId) {
		return userAdditionEntityRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(userAddition -> userAddition.siteId.toString())
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
	}

	@Override
	public void deleteByCorrelationId(String correlationId) {
		userAdditionJobEntityRepository.findByCorrelationId(UUID.fromString(correlationId))
			.ifPresent(x -> userAdditionEntityRepository.deleteById(x.userAdditionId));
	}

	@Override
	public boolean existsByUserIdAndProjectId(FenixUserId userId, String projectId) {
		return userAdditionEntityRepository.existsByProjectIdAndUserId(UUID.fromString(projectId), userId.id);
	}

	@Override
	public void deleteAll() {
		userAdditionEntityRepository.deleteAll();
	}

	@Override
	public void delete(UserAddition userAddition) {
		userAdditionEntityRepository.deleteById(UUID.fromString(userAddition.id));
	}

	@Override
	public void update(UserAdditionJob userAdditionJob) {
		userAdditionJobEntityRepository.findByUserAdditionId(UUID.fromString(userAdditionJob.userAdditionId))
			.map(x -> UserAdditionJobEntity.builder()
				.id(x.getId())
				.correlationId(fromString(userAdditionJob.correlationId.id))
				.userAdditionId(fromString(userAdditionJob.userAdditionId))
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
	public boolean isUserAdded(String siteId, String userId) {
		return userAdditionEntityRepository.existsBySiteIdAndUserId(UUID.fromString(siteId), userId);
	}
}
