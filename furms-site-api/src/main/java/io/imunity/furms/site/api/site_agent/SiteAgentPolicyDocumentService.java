/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.site_agent;

import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.domain.sites.SiteExternalId;

public interface SiteAgentPolicyDocumentService {
	void updateUsersPolicyAcceptances(SiteExternalId siteExternalId, UserPolicyAcceptancesWithServicePolicies userPolicyAcceptancesWithServicePolicies);
	void updatePolicyDocument(SiteExternalId siteExternalId, PolicyDocument policyDocument);
	void updatePolicyDocument(SiteExternalId siteExternalId, PolicyDocument policyDocument, String serviceIdentifier);
}
