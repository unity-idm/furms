/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.unity.client.users;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.imunity.furms.unity.client.users.GroupMembersMultiPaginationProvider.GroupSizeTooLargeException;
import io.imunity.furms.unity.client.users.GroupMembersMultiPaginationProvider.GroupsPage;

class GroupMembersMultiPaginationProviderTest {

	@Test
	void shouldProvideSinglePage() {
		// given
		List<String> groups = of(largeGroupName());
		
		// when
		List<GroupsPage> pages = GroupMembersMultiPaginationProvider.get(groups);
		
		// then
		assertThat(pages)
			.hasSize(1).extracting(GroupsPage::getGroups)
			.containsExactly(of(largeGroupName()));
	}
	
	@Test
	void shouldProvideTwoPages() {
		// given
		List<String> groups = of(largeGroupName(), largeGroupName(), largeGroupName(), largeGroupName(), 
				largeGroupName(), largeGroupName(), largeGroupName(), largeGroupName());
		
		// when
		List<GroupsPage> pages = GroupMembersMultiPaginationProvider.get(groups);
		
		// then
		assertThat(pages)
			.hasSize(2).extracting(GroupsPage::getGroups)
			.containsExactly(
					of(largeGroupName(), largeGroupName(), largeGroupName(), largeGroupName(), 
							largeGroupName(), largeGroupName(), largeGroupName()),
					of(largeGroupName())
			);
	}
	
	@Test
	void shouldNotAcceptGroupExceedingLimit() {
		// when
		Exception ex = Assertions.catchException(() -> GroupMembersMultiPaginationProvider.get(of("x".repeat(1024 * 7))));
		
		// then
		assertThat(ex).isInstanceOf(GroupSizeTooLargeException.class);
	}
	
	private String largeGroupName()
	{
		return "x".repeat(1024 - "&groups=".length());
	}

}
