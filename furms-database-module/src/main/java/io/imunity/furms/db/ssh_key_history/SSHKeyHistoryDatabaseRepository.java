/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_history;

import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKeyHistory;
import io.imunity.furms.domain.ssh_keys.SSHKeyHistoryId;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
class SSHKeyHistoryDatabaseRepository implements SSHKeyHistoryRepository {
	private final SSHKeyHistoryEntityRepository repository;

	SSHKeyHistoryDatabaseRepository(SSHKeyHistoryEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<SSHKeyHistory> findBySiteIdAndOwnerIdLimitTo(SiteId siteId, String ownerId, int limit) {
		return repository.findBysiteIdAndSshkeyOwnerIdOrderByOriginationTimeDesc(siteId.id.toString(), ownerId, PageRequest.of(0,
				limit)).stream()
				.map(SSHKeyHistoryEntity::toSSHKeyHistory).collect(Collectors.toList());
	}

	@Override
	public SSHKeyHistoryId create(SSHKeyHistory sshKeyHistory) {
		SSHKeyHistoryEntity sshkeyHistoryEntity = SSHKeyHistoryEntity.builder()
				.siteId(sshKeyHistory.siteId.id)
				.sshkeyFingerprint(sshKeyHistory.sshkeyFingerprint)
				.sshkeyOwnerId(sshKeyHistory.sshkeyOwnerId.id)
				.originationTime(sshKeyHistory.originationTime).build();
		SSHKeyHistoryEntity history = repository.save(sshkeyHistoryEntity);
		return new SSHKeyHistoryId(history.getId());
	}

	@Override
	public void deleteOldestLeaveOnly(SiteId siteId, String ownerId, int leave) {
		repository.deleteOldestLeaveOnly(siteId.id, ownerId, leave);
	}
	
	@Override
	public void deleteLatest(SiteId siteId, String ownerId) {
		repository.deleteLatest(siteId.id, ownerId);
	}

}
