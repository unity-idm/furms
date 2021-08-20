/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.policy_docuemnts;


import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

public interface PolicyDocumentRepository {
	Optional<PolicyDocument> findById(PolicyId id);

	Set<PolicyDocument> findAll();

	Set<PolicyDocumentExtended> findAllByUserId(FenixUserId userId, BiFunction<PolicyId, Integer, LocalDateTime> acceptedGetter);

	Set<PolicyDocument> findAllBySiteId(String siteId);

	Set<PolicyDocument> findAllSitePoliciesByUserId(PersistentId userId);

	Set<PolicyDocument> findAllServicePoliciesByUserId(PersistentId userId);

	PolicyId create(PolicyDocument projectAllocation);

	PolicyId update(PolicyDocument projectAllocation, boolean revision);

	boolean isNamePresent(String siteId, String name);

	void deleteById(PolicyId id);

	void deleteAll();
}

