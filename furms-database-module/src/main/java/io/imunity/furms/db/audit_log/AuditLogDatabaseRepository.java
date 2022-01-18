/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.audit_log;

import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
class AuditLogDatabaseRepository implements AuditLogRepository {

	private final AuditLogEntityRepository repository;
	private final UsersDAO usersDAO;

	AuditLogDatabaseRepository(AuditLogEntityRepository repository, UsersDAO usersDAO) {
		this.repository = repository;
		this.usersDAO = usersDAO;
	}

	@Override
	public Set<AuditLog> findBy(LocalDateTime from, LocalDateTime to, Set<String> originatorIds, Set<Integer> actionIds, Set<Integer> operationIds, String subject) {
		Map<String, FURMSUser> users = usersDAO.getAllUsers().stream()
			.filter(usr -> usr.fenixUserId.isPresent())
			.collect(Collectors.toMap(x -> x.fenixUserId.get().id, Function.identity()));
		return repository.findByCreationTimeBetweenAndOriginatorIdInAndOperationActionInAndOperationCategoryInAndOperationSubjectContaining(from, to, originatorIds, actionIds, operationIds, subject).stream()
			.map(auditLog -> AuditLog.builder()
				.utcTimestamp(auditLog.creationTime)
				.originator(users.get(auditLog.originatorId))
				.operationCategory(Operation.valueOf(auditLog.operationCategory))
				.action(Action.valueOf(auditLog.operationAction))
				.operationSubject(auditLog.operationSubject)
				.dataJson(auditLog.dataJson)
				.build()
			).collect(Collectors.toSet());
	}

	@Override
	public void create(AuditLog auditLog) {
		repository.save(AuditLogEntity.builder()
				.creationTime(auditLog.utcTimestamp)
				.originatorId(auditLog.originator.fenixUserId.get().id)
				.operationCategory(auditLog.operationCategory.getPersistentId())
				.operationAction(auditLog.action.getPersistentId())
				.operationSubject(auditLog.operationSubject)
				.dataJson(auditLog.dataJson)
				.build()
		);
	}
}