/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.unity.client.users;

import java.util.ArrayList;
import java.util.List;

class GroupMembersMultiPaginationProvider
{
	static List<GroupsPage> get(List<String> allGroups)
	{
		List<GroupsPage> pages = new ArrayList<>();
		
		GroupsPage page = new GroupsPage();
		pages.add(page);
		for (String group: allGroups) {
			if (page.isExceedsLimitWith(group)) {
				page = new GroupsPage();
				pages.add(page);
			}
			page.add(group);
		}
		return pages;
	}
	
	static class GroupsPage
	{
		private static final int MAX_URL_GROUPS_PARAM_SIZE = 7 * 1024;
		private static final int GROUPS_PARAM_KEY_SIZE = "&groups=".length();
		
		private final List<String> groups = new ArrayList<>();
		
		private boolean isExceedsLimitWith(String group) {
			return (calculateTotalGroupsParamSize() + sizeOfParamForGroup(group)) > MAX_URL_GROUPS_PARAM_SIZE;
		}
		
		private void add(String group) {
			if (sizeOfParamForGroup(group) > MAX_URL_GROUPS_PARAM_SIZE)
				throw new GroupSizeTooLargeException(group);
			groups.add(group);
		}
		
		List<String> getGroups() {
			return List.copyOf(groups);
		}
		
		private int sizeOfParamForGroup(String group) {
			return group.length() + GROUPS_PARAM_KEY_SIZE;
		}

		private int calculateTotalGroupsParamSize() {
			return sizeOfGroups() + sizeOfParamKeys();
		}

		private int sizeOfGroups() {
			return groups.stream().map(String::length).mapToInt(Integer::valueOf).sum();
		}

		private int sizeOfParamKeys() {
			return groups.size() * GROUPS_PARAM_KEY_SIZE;
		}
	}
	
	static class GroupSizeTooLargeException extends IllegalArgumentException {
		GroupSizeTooLargeException(String group) {
			super(group);
		}
	}
}