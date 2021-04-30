/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionStatus;
import io.imunity.furms.domain.user_operation.UserRemoval;
import io.imunity.furms.domain.user_operation.UserRemovalStatus;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toSet;

@Repository
class UserOperationDatabaseRepository implements UserOperationRepository {

	private final UserAdditionEntityRepository userAdditionEntityRepository;
	private final UserRemovalEntityRepository userRemovalEntityRepository;

	UserOperationDatabaseRepository(UserAdditionEntityRepository userAdditionEntityRepository, UserRemovalEntityRepository userRemovalEntityRepository) {
		this.userAdditionEntityRepository = userAdditionEntityRepository;
		this.userRemovalEntityRepository = userRemovalEntityRepository;
	}

	@Override
	public Set<UserAddition> findAllUserAdditions(String projectId, String userId) {
		return userAdditionEntityRepository.findAllByProjectIdAndUserId(UUID.fromString(projectId), userId).stream()
			.map(UserAdditionReadEntity::toUserAddition)
			.collect(toSet());
	}

	@Override
	public String create(UserAddition userAddition) {
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.correlationId(fromString(userAddition.correlationId.id))
				.siteId(fromString(userAddition.siteId.id))
				.projectId(fromString(userAddition.projectId))
				.userId(userAddition.userId)
				.uid(userAddition.uid)
				.status(userAddition.status)
				.build()
		);
		return userAdditionSaveEntity.getId().toString();
	}

	@Override
	public String create(UserRemoval userRemoval) {
		UserRemovalSaveEntity userRemovalSaveEntity = userRemovalEntityRepository.save(
			UserRemovalSaveEntity.builder()
				.correlationId(fromString(userRemoval.correlationId.id))
				.siteId(fromString(userRemoval.siteId.id))
				.projectId(fromString(userRemoval.projectId))
				.userAdditionId(fromString(userRemoval.userAdditionId))
				.userId(userRemoval.userId)
				.uid(userRemoval.uid)
				.status(userRemoval.status)
				.build()
		);
		return userRemovalSaveEntity.getId().toString();
	}

	@Override
	public void update(UserAddition userAddition) {
		userAdditionEntityRepository.findByCorrelationId(UUID.fromString(userAddition.correlationId.id))
			.map(oldEntity -> UserAdditionSaveEntity.builder()
				.id(oldEntity.getId())
				.correlationId(oldEntity.correlationId)
				.siteId(oldEntity.siteId)
				.userId(oldEntity.userId)
				.projectId(oldEntity.projectId)
				.uid(userAddition.uid)
				.status(userAddition.status)
				.build())
			.ifPresent(userAdditionEntityRepository::save);
	}

	@Override
	public void updateStatus(CorrelationId correlationId, UserRemovalStatus userRemovalStatus) {
		userRemovalEntityRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(oldEntity -> UserRemovalSaveEntity.builder()
				.id(oldEntity.getId())
				.correlationId(oldEntity.correlationId)
				.siteId(oldEntity.siteId)
				.projectId(oldEntity.projectId)
				.userId(oldEntity.userId)
				.userAdditionId(oldEntity.userAdditionId)
				.uid(oldEntity.uid)
				.status(userRemovalStatus)
				.build())
			.ifPresent(userRemovalEntityRepository::save);
	}

	@Override
	public void updateStatus(CorrelationId correlationId, UserAdditionStatus userAdditionStatus) {
		userAdditionEntityRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(oldEntity -> UserAdditionSaveEntity.builder()
				.id(oldEntity.getId())
				.correlationId(oldEntity.correlationId)
				.siteId(oldEntity.siteId)
				.userId(oldEntity.userId)
				.projectId(oldEntity.projectId)
				.uid(oldEntity.uid)
				.status(userAdditionStatus)
				.build())
			.ifPresent(userAdditionEntityRepository::save);
	}
}
