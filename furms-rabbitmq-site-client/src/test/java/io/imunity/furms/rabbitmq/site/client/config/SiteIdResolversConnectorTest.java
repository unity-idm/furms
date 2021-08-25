/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.imunity.furms.rabbitmq.site.client.message_resolvers_conector.SiteIdResolversConnector;
import io.imunity.furms.rabbitmq.site.models.Body;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SiteIdResolversConnectorTest {
	@Autowired
	private Map<Class<? extends Body>, SiteIdResolversConnector> connectorsMap;

	@Test
	void shouldAllMessageClassHasOwnConnector() throws ClassNotFoundException {
		ClassPathScanningCandidateComponentProvider scanner =
			new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(JsonTypeName.class));
		Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents("io.imunity.furms.rabbitmq.site.models").stream()
			.filter(x -> !x.getBeanClassName().endsWith("Request"))
			.filter(x -> !x.getBeanClassName().endsWith("Update"))
			.filter(x -> !x.getBeanClassName().contains("AgentProjectRemoval"))
			.collect(Collectors.toSet());

		for (BeanDefinition bd : candidateComponents) {
			Class<?> clazz = Class.forName(bd.getBeanClassName());
			assertNotNull(connectorsMap.get(clazz));
		}
	}
}
