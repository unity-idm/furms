/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.policy_documents;

import io.imunity.furms.domain.policy_documents.PolicyAgreement;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Optional;
import java.util.Set;

public interface PolicyDocumentService {

	Optional<PolicyDocument> findById(String siteId, PolicyId id);

	Set<PolicyDocument> findAllBySiteId(String siteId);

	Set<PolicyDocumentExtended> findAllByUserId(FenixUserId userId);

	void create(PolicyDocument policyDocument);

	void addUserPolicyAgreement(FenixUserId userId, PolicyAgreement policyAgreement);

	void update(PolicyDocument policyDocument);

	void updateWithRevision(PolicyDocument policyDocument);

	void delete(String siteId, PolicyId id);
}
