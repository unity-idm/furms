/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_site_access;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UserSiteAccessEntityRepositoryTest extends DBIntegrationTest {
	@Autowired
	private UserSiteAccessEntityRepository userSiteAccessEntityRepository;

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;

	private SiteId siteId;
	private ProjectId projectId;

	private SiteId siteId1;
	private ProjectId projectId1;

	@BeforeEach
	void setUp() {
		userSiteAccessEntityRepository.deleteAll();

		Site site = Site.builder()
			.name("name")
			.build();

		siteId = siteRepository.create(site, new SiteExternalId("id"));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		CommunityId communityId = communityRepository.create(community);

		Project project = Project.builder()
			.communityId(communityId)
			.name("name")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();
		projectId = projectRepository.create(project);

		Site site1 = Site.builder()
			.name("name1")
			.build();

		siteId1 = siteRepository.create(site1, new SiteExternalId("id1"));

		Community community1 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		CommunityId communityId1 = communityRepository.create(community1);

		Project project1 = Project.builder()
			.communityId(communityId1)
			.name("name1")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();
		projectId1 = projectRepository.create(project1);
	}

	@Test
	void shouldCreate(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId.id, projectId.id, "userId");
		UserSiteAccessEntity saved = userSiteAccessEntityRepository.save(userSiteAccessEntity);

		assertEquals(saved, userSiteAccessEntityRepository.findById(saved.getId()).get());
	}

	@Test
	void shouldDeleteById(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId.id, projectId.id, "userId");
		UserSiteAccessEntity saved = userSiteAccessEntityRepository.save(userSiteAccessEntity);

		userSiteAccessEntityRepository.deleteById(userSiteAccessEntity.getId());

		assertTrue(userSiteAccessEntityRepository.findById(saved.getId()).isEmpty());
	}

	@Test
	void shouldDeleteBySiteIdAndProjectIdAndUserId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId.id, projectId.id, "userId");
		UserSiteAccessEntity saved = userSiteAccessEntityRepository.save(userSiteAccessEntity);

		userSiteAccessEntityRepository.deleteBy(siteId.id, projectId.id, "userId");

		assertTrue(userSiteAccessEntityRepository.findById(saved.getId()).isEmpty());
	}

	@Test
	void shouldDeleteByProjectIdAndUserId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId.id, projectId.id, "userId");
		UserSiteAccessEntity saved = userSiteAccessEntityRepository.save(userSiteAccessEntity);
		UserSiteAccessEntity userSiteAccessEntity1 = new UserSiteAccessEntity(siteId1.id, projectId.id, "userId");
		UserSiteAccessEntity saved1 = userSiteAccessEntityRepository.save(userSiteAccessEntity1);

		userSiteAccessEntityRepository.deleteBy(projectId.id, "userId");

		assertTrue(userSiteAccessEntityRepository.findById(saved.getId()).isEmpty());
		assertTrue(userSiteAccessEntityRepository.findById(saved1.getId()).isEmpty());
	}

	@Test
	void shouldFindAllBySiteIdAndUserId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId.id, projectId.id, "userId");
		UserSiteAccessEntity saved = userSiteAccessEntityRepository.save(userSiteAccessEntity);

		UserSiteAccessEntity userSiteAccessEntity1 = new UserSiteAccessEntity(siteId.id, projectId1.id, "userId");
		UserSiteAccessEntity saved1 = userSiteAccessEntityRepository.save(userSiteAccessEntity1);

		UserSiteAccessEntity userSiteAccessEntity2 = new UserSiteAccessEntity(siteId1.id, projectId1.id, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity2);

		Set<UserSiteAccessEntity> entities = userSiteAccessEntityRepository.findAllBySiteIdAndUserId(siteId.id, "userId");

		assertEquals(Set.of(saved, saved1), entities);
	}

	@Test
	void shouldFindAllByProjectId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId.id, projectId.id, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity);

		UserSiteAccessEntity userSiteAccessEntity1 = new UserSiteAccessEntity(siteId.id, projectId1.id, "userId");
		UserSiteAccessEntity saved1 = userSiteAccessEntityRepository.save(userSiteAccessEntity1);

		UserSiteAccessEntity userSiteAccessEntity2 = new UserSiteAccessEntity(siteId1.id, projectId1.id, "userId");
		UserSiteAccessEntity saved2 = userSiteAccessEntityRepository.save(userSiteAccessEntity2);

		Set<UserSiteAccessEntity> entities = userSiteAccessEntityRepository.findAllByProjectId(projectId1.id);

		assertEquals(Set.of(saved1, saved2), entities);
	}

	@Test
	void shouldExistsBySiteIdAndProjectIdAndUserId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId.id, projectId.id, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity);

		UserSiteAccessEntity userSiteAccessEntity1 = new UserSiteAccessEntity(siteId.id, projectId1.id, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity1);

		UserSiteAccessEntity userSiteAccessEntity2 = new UserSiteAccessEntity(siteId1.id, projectId1.id, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity2);

		boolean exists = userSiteAccessEntityRepository.existsBySiteIdAndProjectIdAndUserId(siteId.id, projectId1.id, "userId");

		assertTrue(exists);
	}

	@Test
	void shouldNotExistsBySiteIdAndProjectIdAndUserId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId.id, projectId.id, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity);

		UserSiteAccessEntity userSiteAccessEntity1 = new UserSiteAccessEntity(siteId.id, projectId1.id, "userId1");
		userSiteAccessEntityRepository.save(userSiteAccessEntity1);

		UserSiteAccessEntity userSiteAccessEntity2 = new UserSiteAccessEntity(siteId1.id, projectId1.id, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity2);

		boolean exists = userSiteAccessEntityRepository.existsBySiteIdAndProjectIdAndUserId(siteId.id, projectId1.id, "userId");

		assertFalse(exists);
	}

	@Test
	void shouldNotDuplicateSiteIdAndProjectIdAndUserId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId.id, projectId.id, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity);

		assertThrows(
			DbActionExecutionException.class,
			() -> userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId.id, projectId.id, "userId"))
		);
	}
}
