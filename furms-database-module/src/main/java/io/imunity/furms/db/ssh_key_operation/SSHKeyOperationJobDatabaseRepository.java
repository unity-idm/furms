/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_operation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;
import io.imunity.furms.spi.ssh_key_installation.SSHKeyOperationRepository;

@Repository
class SSHKeyOperationJobDatabaseRepository implements SSHKeyOperationRepository {
	private final SSHKeyOperationJobEntityRepository repository;

	SSHKeyOperationJobDatabaseRepository(SSHKeyOperationJobEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public SSHKeyOperationJob findByCorrelationId(CorrelationId correlationId) {
		SSHKeyOperationJobEntity job = repository.findByCorrelationId(UUID.fromString(correlationId.id));
		return SSHKeyOperationJob.builder().id(job.getId().toString())
				.correlationId(new CorrelationId(job.correlationId.toString()))
				.siteId(job.siteId.toString()).sshkeyId(job.sshkeyId.toString())
				.operation(job.operation).status(job.status).build();
	}

	@Override
	public SSHKeyOperationJob findBySSHKeyIdAndSiteId(String sshkeyId, String siteId) {
		SSHKeyOperationJobEntity job = repository.findBySshkeyIdAndSiteId(UUID.fromString(sshkeyId),
				UUID.fromString(siteId));
		return SSHKeyOperationJob.builder().id(job.getId().toString())
				.correlationId(new CorrelationId(job.correlationId.toString()))
				.siteId(job.siteId.toString()).sshkeyId(job.sshkeyId.toString())
				.operation(job.operation).status(job.status).build();
	}

	@Override
	public String create(SSHKeyOperationJob sshkeyOperationJob) {
		SSHKeyOperationJobEntity sshkeyInstallationJobEntity = SSHKeyOperationJobEntity.builder()
				.correlationId(UUID.fromString(sshkeyOperationJob.correlationId.id))
				.siteId(UUID.fromString(sshkeyOperationJob.siteId))
				.sshkeyId(UUID.fromString(sshkeyOperationJob.sshkeyId))
				.status(sshkeyOperationJob.status).operation(sshkeyOperationJob.operation).build();
		SSHKeyOperationJobEntity job = repository.save(sshkeyInstallationJobEntity);
		return job.getId().toString();
	}

	@Override
	public String update(String id, SSHKeyOperationStatus status, Optional<String> error) {
		repository.findById(UUID.fromString(id))
				.map(job -> SSHKeyOperationJobEntity.builder().id(job.getId())
						.correlationId(job.correlationId).siteId(job.siteId)
						.sshkeyId(job.sshkeyId).operation(job.operation)
						.status(status).error(error.orElse(null)).build())
				.ifPresent(repository::save);
		return id;
	}

	@Override
	public void delete(String id) {
		repository.deleteById(UUID.fromString(id));
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	@Override
	public void deleteBySSHKeyIdAndSiteId(String sshkeyId, String siteId) {
		repository.deleteBySshKeyIdAndSiteId(UUID.fromString(sshkeyId), UUID.fromString(siteId));

	}

	@Override
	public List<SSHKeyOperationJob> findBySSHKey(String sshkeyId) {
		return repository.findBySshkeyId(sshkeyId).stream()
				.map(job -> SSHKeyOperationJob.builder().id(job.getId().toString())
						.correlationId(new CorrelationId(job.correlationId.toString()))
						.siteId(job.siteId.toString()).sshkeyId(job.sshkeyId.toString())
						.operation(job.operation).status(job.status).build())
				.collect(Collectors.toList());
	}
}
