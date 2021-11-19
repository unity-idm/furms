/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_remover;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectUpdateRequest;
import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.site.api.message_remover.ProjectInstallationRemover;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
class ProjectInstallationRemoveStrategy implements PendingMessageRemoveStrategy {
	private final ProjectInstallationRemover projectInstallationRemover;
	private final Set<Class<? extends Body>> classes = Set.of(AgentProjectInstallationRequest.class, AgentProjectUpdateRequest.class);

	ProjectInstallationRemoveStrategy(ProjectInstallationRemover projectInstallationRemover) {
		this.projectInstallationRemover = projectInstallationRemover;
	}

	@Override
	public boolean isApplicable(String name) {
		return classes.stream()
			.map(x -> x.getAnnotation(JsonTypeName.class).value())
			.anyMatch(x -> x.equals(name));
	}

	@Override
	public void remove(CorrelationId correlationId) {
		projectInstallationRemover.remove(correlationId);
	}
}
