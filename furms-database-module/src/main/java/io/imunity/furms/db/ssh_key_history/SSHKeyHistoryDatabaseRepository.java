/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_history;

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
	public List<SSHKeyHistory> findLastBySSHKeyIdLimitTo(String siteId, int limit) {
		return repository.findBysiteIdOrderByOriginationTimeDesc(siteId, PageRequest.of(0, limit)).stream()
				.map(SSHKeyHistoryEntity::toSSHKeyHistory).collect(Collectors.toList());
	}

	@Override
	public String create(SSHKeyHistory sshKeyHistory) {
		SSHKeyHistoryEntity sshkeyHistoryEntity = SSHKeyHistoryEntity.builder()
				.siteId(UUID.fromString(sshKeyHistory.siteId))
				.sshkeyFingerprint(sshKeyHistory.sshkeyFingerprint)
				.originationTime(sshKeyHistory.originationTime).build();
		SSHKeyHistoryEntity history = repository.save(sshkeyHistoryEntity);
		return history.getId().toString();
	}

	@Override
	public void deleteOldestLeaveOnly(String siteId, int leave) {
		repository.deleteOldestLeaveOnly(siteId, leave);
	}

}
