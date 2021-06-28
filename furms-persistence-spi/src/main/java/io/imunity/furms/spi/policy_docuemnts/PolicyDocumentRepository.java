/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.policy_docuemnts;


import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;

import java.util.Optional;
import java.util.Set;

public interface PolicyDocumentRepository {
	Optional<PolicyDocument> findById(PolicyId id);

	Set<PolicyDocument> findAllBySiteId(String siteId);

	PolicyId create(PolicyDocument projectAllocation);

	PolicyId update(PolicyDocument projectAllocation);


	boolean isNamePresent(String communityId, String name);

	void deleteById(PolicyId id);

	void deleteAll();
}

