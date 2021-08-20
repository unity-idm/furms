/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.policy_documents;

import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceAtSite;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;

import java.util.Optional;
import java.util.Set;

public interface PolicyDocumentService {

	Optional<PolicyDocument> findById(String siteId, PolicyId id);

	Set<PolicyDocument> findAll();

	Set<PolicyDocument> findAllBySiteId(String siteId);

	Set<FURMSUser> findAllUsersWithoutCurrentRevisionPolicyAcceptance(String siteId, PolicyId policyId);

	Set<UserPolicyAcceptances> findAllUsersPolicyAcceptances(String siteId);

	Set<PolicyDocumentExtended> findAllByCurrentUser();

	Set<PolicyAcceptanceAtSite> findSitePolicyAcceptancesByUserId(PersistentId userId);

	Set<PolicyAcceptanceAtSite> findServicesPolicyAcceptancesByUserId(PersistentId userId);

	void create(PolicyDocument policyDocument);

	void addCurrentUserPolicyAcceptance(PolicyAcceptance policyAcceptance);

	void addUserPolicyAcceptance(String siteId, FenixUserId userId, PolicyAcceptance policyAcceptance);

	void update(PolicyDocument policyDocument);

	void updateWithRevision(PolicyDocument policyDocument);

	void delete(String siteId, PolicyId id);
}
