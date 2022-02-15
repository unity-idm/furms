/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.performance.tests.data;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.PersistentId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static io.imunity.furms.performance.tests.data.DataLoaderUtils.randomAcronym;
import static io.imunity.furms.performance.tests.data.DataLoaderUtils.randomName;
import static java.util.stream.Collectors.toSet;

class CommunityDataLoader {

	private final CommunityService communityService;
	private final ProjectService projectService;

	CommunityDataLoader(CommunityService communityService, ProjectService projectService) {
		this.communityService = communityService;
		this.projectService = projectService;
	}

	Set<Data.Community> loadCommunities(final long communitiesCount,
	                                    final long projectsPerCommunityCount,
	                                    final Set<Data.User> users) {
		final List<String> names = LongStream.range(0, communitiesCount)
				.mapToObj(i -> {
					final String name = randomName();
					communityService.create(Community.builder()
							.name(name)
							.description(UUID.randomUUID().toString())
							.logo(FurmsImage.empty())
							.build());
					return name;
				})
				.collect(Collectors.toList());

		final List<Data.User> leaders = new ArrayList<>(users);
		if(leaders.isEmpty())
			throw new IllegalArgumentException("There are no users to choose a Project leader");
		Random randomLeader = new Random();
		int leaderSize = leaders.size();

		return communityService.findAll().stream()
				.filter(community -> names.contains(community.getName()))
				.map(community -> new Data.Community(
						community.getId(),
						LongStream.range(0, projectsPerCommunityCount)
								.mapToObj(i -> projectService.create(Project.builder()
										.communityId(community.getId())
										.name(randomName())
										.acronym(randomAcronym())
										.researchField(UUID.randomUUID().toString())
										.utcStartTime(LocalDateTime.now())
										.utcEndTime(LocalDateTime.now().plusYears(1))
										.leaderId(new PersistentId(leaders.get(randomLeader.nextInt(leaderSize)).persistentId))
										.build()))
								.collect(toSet())))
				.collect(toSet());
	}

}
