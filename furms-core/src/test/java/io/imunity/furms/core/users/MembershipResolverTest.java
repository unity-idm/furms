/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.core.users;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.CommunityMembership;
import io.imunity.furms.domain.users.ProjectMembership;
import io.imunity.furms.domain.users.UserAttribute;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MembershipResolverTest {

	private final CommunityRepository communitiesDAO = mock(CommunityRepository.class);
	private final ProjectRepository projectsDAO = mock(ProjectRepository.class);
	
	private final UUID COMMUNITY1_ID = UUID.randomUUID();
	private final UUID COMMUNITY2_ID = UUID.randomUUID();
	private final UUID PROJECT1_ID = UUID.randomUUID();
	private final UUID PROJECT2_ID = UUID.randomUUID();
	
	@Test
	public void shouldResolveTwoCommunitiesWithProjects() {
		MembershipResolver resolver = new MembershipResolver(communitiesDAO, projectsDAO);
		Set<UserAttribute> c1Attributes = Set.of(new UserAttribute("a1", "a1Val"));
		Set<UserAttribute> c2Attributes = Set.of(new UserAttribute("a4", "a4Val"));
		Set<UserAttribute> p1Attributes = Set.of(new UserAttribute("a2", "a2Val"));
		Set<UserAttribute> p2Attributes = Set.of(new UserAttribute("a3", "a3Val"));
		Map<ResourceId, Set<UserAttribute>> attrByResource = Map.of(
				new ResourceId(new CommunityId(COMMUNITY1_ID), ResourceType.COMMUNITY), c1Attributes,
				new ResourceId(new CommunityId(COMMUNITY2_ID), ResourceType.COMMUNITY), c2Attributes,
				new ResourceId(new ProjectId(PROJECT1_ID), ResourceType.PROJECT), p1Attributes,
				new ResourceId(new ProjectId(PROJECT2_ID), ResourceType.PROJECT), p2Attributes);
		when(communitiesDAO.findById(new CommunityId(COMMUNITY1_ID))).thenReturn(Optional.of(
				Community.builder().id(COMMUNITY1_ID.toString()).name("c1").build()));
		when(communitiesDAO.findById(new CommunityId(COMMUNITY2_ID))).thenReturn(Optional.of(
				Community.builder().id(COMMUNITY2_ID.toString()).name("c2").build()));
		when(projectsDAO.findAllByCommunityId(new CommunityId(COMMUNITY1_ID))).thenReturn(Set.of(
				Project.builder().id(PROJECT1_ID.toString()).name("p1").build()));
		when(projectsDAO.findAllByCommunityId(new CommunityId(COMMUNITY2_ID))).thenReturn(Set.of(
				Project.builder().id(PROJECT2_ID.toString()).name("p2").build()));
		
		
		Set<CommunityMembership> membership = resolver.resolveCommunitiesMembership(attrByResource);
		
		assertThat(membership).containsOnly(
				new CommunityMembership(COMMUNITY1_ID.toString(), "c1", 
						Set.of(new ProjectMembership(new ProjectId(PROJECT1_ID), "p1", p1Attributes)),
						c1Attributes),
				new CommunityMembership(COMMUNITY2_ID.toString(), "c2", 
						Set.of(new ProjectMembership(new ProjectId(PROJECT2_ID), "p2", p2Attributes)),
						c2Attributes));
	}

	@Test
	public void shouldFilterSysAttributes() {
		MembershipResolver resolver = new MembershipResolver(communitiesDAO, projectsDAO);
		Set<UserAttribute> c1Attributes = Set.of(new UserAttribute("sys:a1", "a1Val"));
		Set<UserAttribute> p1Attributes = Set.of(new UserAttribute("sys:a2", "a2Val"));
		Map<ResourceId, Set<UserAttribute>> attrByResource = Map.of(
				new ResourceId(new CommunityId(COMMUNITY1_ID), ResourceType.COMMUNITY), c1Attributes,
				new ResourceId(new ProjectId(PROJECT1_ID), ResourceType.PROJECT), p1Attributes);
		when(communitiesDAO.findById(new CommunityId(COMMUNITY1_ID))).thenReturn(Optional.of(
				Community.builder().id(COMMUNITY1_ID.toString()).name("c1").build()));
		when(projectsDAO.findAllByCommunityId(new CommunityId(COMMUNITY1_ID))).thenReturn(Set.of(
				Project.builder().id(PROJECT1_ID.toString()).name("p1").build()));
		
		Set<CommunityMembership> membership = resolver.resolveCommunitiesMembership(attrByResource);
		
		assertThat(membership).containsOnly(
				new CommunityMembership(COMMUNITY1_ID.toString(), "c1", 
						Set.of(new ProjectMembership(new ProjectId(PROJECT1_ID), "p1", Collections.emptySet())),
						Collections.emptySet()));
	}
	
	@Test
	public void shouldResolveCommunityWithoutProjects() {
		MembershipResolver resolver = new MembershipResolver(communitiesDAO, projectsDAO);
		Set<UserAttribute> c1Attributes = Collections.emptySet();
		Map<ResourceId, Set<UserAttribute>> attrByResource = Map.of(
				new ResourceId(new CommunityId(COMMUNITY1_ID), ResourceType.COMMUNITY), c1Attributes);
		when(communitiesDAO.findById(new CommunityId(COMMUNITY1_ID))).thenReturn(Optional.of(
				Community.builder().id(COMMUNITY1_ID.toString()).name("c1").build()));
		when(projectsDAO.findAllByCommunityId(new CommunityId(COMMUNITY1_ID))).thenReturn(Collections.emptySet());
		
		Set<CommunityMembership> membership = resolver.resolveCommunitiesMembership(attrByResource);
		
		assertThat(membership).containsOnly(
				new CommunityMembership(COMMUNITY1_ID.toString(), "c1", 
						Collections.emptySet(), 
						c1Attributes));
	}

	
	@Test
	public void shouldIgnoreMissingCommunity() {
		MembershipResolver resolver = new MembershipResolver(communitiesDAO, projectsDAO);
		Map<ResourceId, Set<UserAttribute>> attrByResource = Map.of();
		
		Set<CommunityMembership> membership = resolver.resolveCommunitiesMembership(attrByResource);
		
		assertThat(membership).isEmpty();
	}


	@Test
	public void shouldIgnoreMissingProject() {
		MembershipResolver resolver = new MembershipResolver(communitiesDAO, projectsDAO);
		Set<UserAttribute> c1Attributes = Collections.emptySet();
		Set<UserAttribute> p1Attributes = Collections.emptySet();
		Map<ResourceId, Set<UserAttribute>> attrByResource = Map.of(
				new ResourceId(new CommunityId(COMMUNITY1_ID), ResourceType.COMMUNITY), c1Attributes,
				new ResourceId(new ProjectId(PROJECT1_ID), ResourceType.PROJECT), p1Attributes);
		when(communitiesDAO.findById(new CommunityId(COMMUNITY1_ID))).thenReturn(Optional.of(
				Community.builder().id(COMMUNITY1_ID.toString()).name("c1").build()));
		when(projectsDAO.findAllByCommunityId(new CommunityId(COMMUNITY1_ID))).thenReturn(Set.of(
				Project.builder().id(PROJECT2_ID.toString()).name("p2").build()));
		
		
		Set<CommunityMembership> membership = resolver.resolveCommunitiesMembership(attrByResource);
		
		assertThat(membership).containsOnly(
				new CommunityMembership(COMMUNITY1_ID.toString(), "c1", 
						Collections.emptySet(), 
						c1Attributes));

	}
}
