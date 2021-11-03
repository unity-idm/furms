/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.policy_docuemnts;


import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.AssignedPolicyDocument;
import io.imunity.furms.domain.users.FenixUserId;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

public interface PolicyDocumentRepository {
	Optional<PolicyDocument> findById(PolicyId id);

	Optional<PolicyDocument> findByUserGrantId(String userGrantId);

	Optional<PolicyDocument> findSitePolicy(String siteId);

	Set<PolicyDocument> findAll();

	Map<FenixUserId, Set<PolicyDocument>> findAllUsersPolicies(String siteId);

	Set<FenixUserId> findAllPolicyUsers(String siteId, PolicyId policyId);

	Set<PolicyDocumentExtended> findAllByUserId(FenixUserId userId, BiFunction<PolicyId, Integer, LocalDateTime> acceptedGetter);

	Set<PolicyDocument> findAllBySiteId(String siteId);

	Set<AssignedPolicyDocument> findAllAssignPoliciesBySiteId(String siteId);

	Set<PolicyDocument> findAllSitePoliciesByUserId(FenixUserId userId);

	Set<PolicyDocument> findAllServicePoliciesByUserId(FenixUserId userId);

	PolicyId create(PolicyDocument policyDocument);

	PolicyId update(PolicyDocument policyDocument, boolean revision);

	boolean isNamePresent(String siteId, String name);

	void deleteById(PolicyId id);

	void deleteAll();
}

