/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.policy_documents;

import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.unity.client.users.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UnityPolicyDocumentDAOTest {

	@Mock
	private UserService userService;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private ResourceAccessRepository resourceAccessRepository;

	@InjectMocks
	private UnityPolicyDocumentDAO unityPolicyDocumentDAO;

	@Test
	void shouldGetUserPolicyAcceptances() {
		String siteId = "siteId";
		String userId = "userId1";
		Map<String, Set<String>> map = Map.of("id", Set.of("id"));
		FURMSUser furmsUser1 = FURMSUser.builder()
			.fenixUserId(userId)
			.email("email1")
			.build();
		FURMSUser furmsUser2 = FURMSUser.builder()
			.fenixUserId("userId2")
			.email("email2")
			.build();
		UserPolicyAcceptances userPolicyAcceptances1 = new UserPolicyAcceptances(furmsUser1, Set.of());
		UserPolicyAcceptances userPolicyAcceptances2 = new UserPolicyAcceptances(furmsUser2, Set.of());

		when(siteRepository.findRelatedProjectIds(new SiteId(siteId))).thenReturn(map);
		when(userService.getAllUsersPolicyAcceptanceFromGroups(map)).thenReturn(Set.of(userPolicyAcceptances1, userPolicyAcceptances2));
		when(resourceAccessRepository.findUsersBySiteId(siteId)).thenReturn(Set.of(new FenixUserId(userId)));


		Set<UserPolicyAcceptances> userPolicyAcceptances = unityPolicyDocumentDAO.getUserPolicyAcceptances(siteId);

		assertEquals(userPolicyAcceptances, Set.of(userPolicyAcceptances1));
	}
}