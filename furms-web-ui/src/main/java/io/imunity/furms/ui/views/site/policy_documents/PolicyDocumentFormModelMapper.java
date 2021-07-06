/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.policy_documents;

import io.imunity.furms.domain.policy_documents.PolicyDocument;

class PolicyDocumentFormModelMapper {

	static PolicyDocumentFormModel map(PolicyDocument policyDocument){
		return PolicyDocumentFormModel.builder()
			.id(policyDocument.id)
			.siteId(policyDocument.siteId)
			.name(policyDocument.name)
			.workflow(policyDocument.workflow)
			.revision(policyDocument.revision)
			.contentType(policyDocument.contentType)
			.wysiwygText(policyDocument.htmlText)
			.policyFile(policyDocument.policyFile)
			.build();
	}

	static PolicyDocument map(PolicyDocumentFormModel policyDocument){
		return PolicyDocument.builder()
			.id(policyDocument.id)
			.siteId(policyDocument.siteId)
			.name(policyDocument.name)
			.workflow(policyDocument.workflow)
			.contentType(policyDocument.contentType)
			.wysiwygText(policyDocument.wysiwygText)
			.file(policyDocument.policyFile)
			.build();
	}
}
