/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJobId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;

@Repository
class SSHKeyOperationJobDatabaseRepository implements SSHKeyOperationRepository {
	private final SSHKeyOperationJobEntityRepository repository;

	SSHKeyOperationJobDatabaseRepository(SSHKeyOperationJobEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<SSHKeyOperationJob> findByStatus(SSHKeyOperationStatus status) {
		return repository.findByStatus(status.toString()).stream()
				.map(job -> SSHKeyOperationJob.builder().id(job.getId().toString())
						.correlationId(new CorrelationId(job.correlationId.toString()))
						.siteId(job.siteId.toString()).sshkeyId(job.sshkeyId.toString())
						.operation(job.operation).status(job.status).error(job.error)
						.originationTime(job.originationTime).build())
				.collect(Collectors.toList());
	}

	@Override
	public List<SSHKeyOperationJob> findAll() {
		return stream(repository.findAll().spliterator(), false)
				.map(job -> SSHKeyOperationJob.builder().id(job.getId().toString())
						.correlationId(new CorrelationId(job.correlationId.toString()))
						.siteId(job.siteId.toString()).sshkeyId(job.sshkeyId.toString())
						.operation(job.operation).status(job.status).error(job.error)
						.originationTime(job.originationTime).build())
				.collect(Collectors.toList());
	}

	@Override
	public SSHKeyOperationJob findByCorrelationId(CorrelationId correlationId) {
		SSHKeyOperationJobEntity job = repository.findByCorrelationId(UUID.fromString(correlationId.id));
		return SSHKeyOperationJob.builder().id(job.getId().toString())
				.correlationId(new CorrelationId(job.correlationId.toString()))
				.siteId(job.siteId.toString()).sshkeyId(job.sshkeyId.toString())
				.operation(job.operation).status(job.status).error(job.error)
				.originationTime(job.originationTime).build();
	}

	@Override
	public SSHKeyOperationJob findBySSHKeyIdAndSiteId(SSHKeyId sshkeyId, SiteId siteId) {
		SSHKeyOperationJobEntity job = repository.findBySshkeyIdAndSiteId(sshkeyId.id,
				siteId.id);
		return SSHKeyOperationJob.builder().id(job.getId().toString())
				.correlationId(new CorrelationId(job.correlationId.toString()))
				.siteId(job.siteId.toString()).sshkeyId(job.sshkeyId.toString())
				.operation(job.operation).status(job.status).error(job.error)
				.originationTime(job.originationTime).build();
	}

	@Override
	public SSHKeyOperationJobId create(SSHKeyOperationJob sshkeyOperationJob) {
		SSHKeyOperationJobEntity sshkeyInstallationJobEntity = SSHKeyOperationJobEntity.builder()
				.correlationId(UUID.fromString(sshkeyOperationJob.correlationId.id))
				.siteId(sshkeyOperationJob.siteId.id)
				.sshkeyId(sshkeyOperationJob.sshkeyId.id)
				.status(sshkeyOperationJob.status).operation(sshkeyOperationJob.operation)
				.error(sshkeyOperationJob.error).originationTime(sshkeyOperationJob.originationTime)
				.build();
		SSHKeyOperationJobEntity job = repository.save(sshkeyInstallationJobEntity);
		return new SSHKeyOperationJobId(job.getId());
	}

	@Override
	public void update(SSHKeyOperationJobId id, SSHKeyOperationStatus status, Optional<String> error,
	                   LocalDateTime originationTime) {
		repository.findById(id.id)
				.map(job -> SSHKeyOperationJobEntity.builder().id(job.getId())
						.correlationId(job.correlationId).siteId(job.siteId)
						.sshkeyId(job.sshkeyId).operation(job.operation).status(status)
						.error(error.orElse(null))
						.originationTime(originationTime)
						.build())
				.ifPresent(repository::save);
	}

	@Override
	public void delete(SSHKeyOperationJobId id) {
		repository.deleteById(id.id);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	@Override
	public void deleteBySSHKeyIdAndSiteId(SSHKeyId sshkeyId, SiteId siteId) {
		repository.deleteBySshKeyIdAndSiteId(sshkeyId.id, siteId.id);

	}

	@Override
	public List<SSHKeyOperationJob> findBySSHKey(SSHKeyId sshkeyId) {
		return repository.findBySshkeyId(sshkeyId.id).stream()
				.map(job -> SSHKeyOperationJob.builder().id(job.getId().toString())
						.correlationId(new CorrelationId(job.correlationId.toString()))
						.siteId(job.siteId.toString()).sshkeyId(job.sshkeyId.toString())
						.operation(job.operation).status(job.status).error(job.error)
						.originationTime(job.originationTime).build())
				.collect(Collectors.toList());
	}

	@Override
	public void delete(CorrelationId id) {
		repository.deleteByCorrelationId(UUID.fromString(id.id));
	}
}
