/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_history;

import static java.util.UUID.fromString;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import io.imunity.furms.domain.ssh_keys.SSHKeyHistory;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;

@Repository
class SSHKeyHistoryDatabaseRepository implements SSHKeyHistoryRepository {
	private final SSHKeyHistoryEntityRepository repository;

	SSHKeyHistoryDatabaseRepository(SSHKeyHistoryEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<SSHKeyHistory> findBySiteIdAndOwnerIdLimitTo(String siteId, String ownerId, int limit) {
		return repository.findBysiteIdAndSshkeyOwnerIdOrderByOriginationTimeDesc(siteId, ownerId, PageRequest.of(0, limit)).stream()
				.map(SSHKeyHistoryEntity::toSSHKeyHistory).collect(Collectors.toList());
	}

	@Override
	public String create(SSHKeyHistory sshKeyHistory) {
		SSHKeyHistoryEntity sshkeyHistoryEntity = SSHKeyHistoryEntity.builder()
				.siteId(UUID.fromString(sshKeyHistory.siteId))
				.sshkeyFingerprint(sshKeyHistory.sshkeyFingerprint)
				.sshkeyOwnerId(sshKeyHistory.sshkeyOwnerId.id)
				.originationTime(sshKeyHistory.originationTime).build();
		SSHKeyHistoryEntity history = repository.save(sshkeyHistoryEntity);
		return history.getId().toString();
	}

	@Override
	public void deleteOldestLeaveOnly(String siteId, String ownerId, int leave) {
		repository.deleteOldestLeaveOnly(fromString(siteId), ownerId, leave);
	}
	
	@Override
	public void deleteLatest(String siteId, String ownerId) {
		repository.deleteLatest(fromString(siteId), ownerId);
	}

}
