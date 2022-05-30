/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.site;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent_pending_messages.SiteAgentPendingMessage;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import io.imunity.furms.site.api.site_agent.SiteAgentRetryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultSite;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SiteProtocolMessageIntegrationTest extends IntegrationTestBase {

	private Site site;
	private Site site1;
	@MockBean
	private SiteAgentRetryService siteAgentRetryService;

	@BeforeEach
	void setUp() {
		Site.SiteBuilder siteBuilder = defaultSite().name("site1");
		site = siteBuilder
				.id(siteRepository.create(siteBuilder.build(), siteBuilder.build().getExternalId()))
				.build();

		Site.SiteBuilder siteBuilder1 = defaultSite().name("site2");
		site1 = siteBuilder1
			.id(siteRepository.create(siteBuilder1.build(), new SiteExternalId("se_i1")))
			.build();
	}

	@Test
	void shouldFindAllProtocolMessageForSpecificSite() throws Exception {
		//given
		CorrelationId correlationId = CorrelationId.randomID();
		CorrelationId correlationId1 = CorrelationId.randomID();

		LocalDateTime sentOn = LocalDate.now().minusDays(10).atStartOfDay().plusSeconds(30);
		LocalDateTime sentOn1 = LocalDate.now().minusDays(5).atStartOfDay().plusSeconds(12);

		LocalDateTime ackOn = LocalDate.now().minusDays(2).atStartOfDay().plusSeconds(20);

		siteAgentPendingMessageRepository.create(SiteAgentPendingMessage.builder()
				.siteExternalId(site.getExternalId())
				.correlationId(correlationId)
				.jsonContent("content1")
				.utcSentAt(sentOn)
				.utcAckAt(ackOn)
				.retryCount(2)
			.build());

		siteAgentPendingMessageRepository.create(SiteAgentPendingMessage.builder()
			.siteExternalId(site.getExternalId())
			.correlationId(correlationId1)
			.jsonContent("content2")
			.utcSentAt(sentOn1)
			.retryCount(2)
			.build());

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/protocolMessages", site.getId().id))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].id", in(List.of(correlationId.id, correlationId1.id))))
				.andExpect(jsonPath("$.[0].jsonContent", in(List.of("content1", "content2"))))
				.andExpect(jsonPath("$.[0].status", in(List.of("SENT", "ACKNOWLEDGED"))))
				.andExpect(jsonPath("$.[0].sentOn", in(List.of(sentOn.atZone(ZoneOffset.UTC).toString(), sentOn1.atZone(ZoneOffset.UTC).toString()))))
				.andExpect(jsonPath("$.[0].ackOn", in(asList(ackOn.atZone(ZoneOffset.UTC).toString(), null))))
				.andExpect(jsonPath("$.[1].id", in(List.of(correlationId.id, correlationId1.id))))
				.andExpect(jsonPath("$.[1].jsonContent", in(List.of("content1", "content2"))))
				.andExpect(jsonPath("$.[1].status", in(List.of("SENT", "ACKNOWLEDGED"))))
				.andExpect(jsonPath("$.[1].sentOn", in(List.of(sentOn.atZone(ZoneOffset.UTC).toString(), sentOn1.atZone(ZoneOffset.UTC).toString()))))
				.andExpect(jsonPath("$.[1].ackOn", in(asList(ackOn.atZone(ZoneOffset.UTC).toString(), null))));
	}

	@Test
	void shouldRemoveProtocolMessageForSpecificSite() throws Exception {
		//given
		CorrelationId correlationId = CorrelationId.randomID();
		LocalDateTime sentOn = LocalDate.now().minusDays(10).atStartOfDay().plusSeconds(30);
		LocalDateTime ackOn = LocalDate.now().minusDays(2).atStartOfDay().plusSeconds(20);

		siteAgentPendingMessageRepository.create(SiteAgentPendingMessage.builder()
			.siteExternalId(site.getExternalId())
			.correlationId(correlationId)
			.jsonContent("{\"body\" : {\"UserProjectAddRequest\" : {}}}")
			.utcSentAt(sentOn)
			.utcAckAt(ackOn)
			.retryCount(2)
			.build());

		//when
		mockMvc.perform(adminDELETE("/rest-api/v1/sites/{siteId}/protocolMessages/{messageId}", site.getId().id, correlationId.id))
			.andDo(print());

		assertEquals(Optional.empty(), siteAgentPendingMessageRepository.find(correlationId));
	}

	@Test
	void shouldRetryProtocolMessageForSpecificSite() throws Exception {
		//given
		CorrelationId correlationId = CorrelationId.randomID();
		LocalDateTime sentOn = LocalDate.now().minusDays(10).atStartOfDay().plusSeconds(30);
		LocalDateTime ackOn = LocalDate.now().minusDays(2).atStartOfDay().plusSeconds(20);

		siteAgentPendingMessageRepository.create(SiteAgentPendingMessage.builder()
			.siteExternalId(site.getExternalId())
			.correlationId(correlationId)
			.jsonContent("content1")
			.utcSentAt(sentOn)
			.utcAckAt(ackOn)
			.retryCount(1)
			.build());

		//when
		mockMvc.perform(adminPOST("/rest-api/v1/sites/{siteId}/protocolMessages/{messageId}", site.getId().id, correlationId.id))
			.andDo(print());

		assertEquals(2, siteAgentPendingMessageRepository.find(correlationId).get().retryCount);
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotExistDuringGettingAllProtocolMessages() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/protocolMessages", UUID.randomUUID().toString()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnNotFoundIfCorrelationIdDoesNotExistDuringRemovingProtocolMessages() throws Exception {
		//when
		mockMvc.perform(delete("/rest-api/v1/sites/{siteId}/protocolMessages/{correlationId}", site.getId().id,
				UUID.randomUUID())
			.with(ADMIN_USER.getHttpBasic()))
			.andDo(print())
			.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnForbiddenIfSiteDoesNotBelongToUserDuringGettingAllProtocolMessages() throws Exception {
		final TestUser testUser = basicUser();
		setupUser(testUser);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/protocolMessages", site.getId().id)
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}


	@Test
	void shouldReturnForbiddenIfSiteDoesNotBelongToUserDuringRemovingProtocolMessages() throws Exception {
		//given
		TestUser testUser = basicUser();
		setupUser(testUser);

		CorrelationId correlationId = CorrelationId.randomID();
		LocalDateTime sentOn = LocalDate.now().minusDays(10).atStartOfDay().plusSeconds(30);
		LocalDateTime ackOn = LocalDate.now().minusDays(2).atStartOfDay().plusSeconds(20);

		siteAgentPendingMessageRepository.create(SiteAgentPendingMessage.builder()
			.siteExternalId(site.getExternalId())
			.correlationId(correlationId)
			.jsonContent("content1")
			.utcSentAt(sentOn)
			.utcAckAt(ackOn)
			.retryCount(2)
			.build());

		//when
		mockMvc.perform(delete("/rest-api/v1/sites/{siteId}/protocolMessages/{messageId}", site.getId().id, correlationId.id)
				.with(testUser.getHttpBasic()))
			.andDo(print())
			.andExpect(status().isForbidden());
	}

	@Test
	void shouldReturnForbiddenIfSiteDoesNotBelongToUserDuringRetryingProtocolMessages() throws Exception {
		//given
		TestUser testUser = basicUser();
		setupUser(testUser);

		CorrelationId correlationId = CorrelationId.randomID();
		LocalDateTime sentOn = LocalDate.now().minusDays(10).atStartOfDay().plusSeconds(30);
		LocalDateTime ackOn = LocalDate.now().minusDays(2).atStartOfDay().plusSeconds(20);

		siteAgentPendingMessageRepository.create(SiteAgentPendingMessage.builder()
			.siteExternalId(site.getExternalId())
			.correlationId(correlationId)
			.jsonContent("content1")
			.utcSentAt(sentOn)
			.utcAckAt(ackOn)
			.retryCount(2)
			.build());

		//when
		mockMvc.perform(post("/rest-api/v1/sites/{siteId}/protocolMessages/{messageId}", site.getId().id, correlationId.id)
				.with(testUser.getHttpBasic()))
			.andDo(print())
			.andExpect(status().isForbidden());
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotBelongToMessageDuringRetryingProtocolMessages() throws Exception {
		//given
		CorrelationId correlationId = CorrelationId.randomID();

		LocalDateTime sentOn = LocalDate.now().minusDays(10).atStartOfDay().plusSeconds(30);

		LocalDateTime ackOn = LocalDate.now().minusDays(2).atStartOfDay().plusSeconds(20);

		siteAgentPendingMessageRepository.create(SiteAgentPendingMessage.builder()
			.siteExternalId(site1.getExternalId())
			.correlationId(correlationId)
			.jsonContent("content1")
			.utcSentAt(sentOn)
			.utcAckAt(ackOn)
			.retryCount(2)
			.build());

		//when
		mockMvc.perform(post("/rest-api/v1/sites/{siteId}/protocolMessages/{messageId}", site.getId().id, correlationId.id)
				.with(ADMIN_USER.getHttpBasic()))
			.andDo(print())
			.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotBelongToMessageDuringRemovingProtocolMessages() throws Exception {
		//given
		CorrelationId correlationId = CorrelationId.randomID();

		LocalDateTime sentOn = LocalDate.now().minusDays(10).atStartOfDay().plusSeconds(30);

		LocalDateTime ackOn = LocalDate.now().minusDays(2).atStartOfDay().plusSeconds(20);

		siteAgentPendingMessageRepository.create(SiteAgentPendingMessage.builder()
			.siteExternalId(site1.getExternalId())
			.correlationId(correlationId)
			.jsonContent("content1")
			.utcSentAt(sentOn)
			.utcAckAt(ackOn)
			.retryCount(2)
			.build());

		//when
		mockMvc.perform(delete("/rest-api/v1/sites/{siteId}/protocolMessages/{messageId}", site.getId().id, correlationId.id)
				.with(ADMIN_USER.getHttpBasic()))
			.andDo(print())
			.andExpect(status().isNotFound());
	}

}
