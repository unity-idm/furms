/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_site_access;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
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
import java.util.UUID;

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

	private UUID siteId;
	private UUID projectId;

	private UUID siteId1;
	private UUID projectId1;

	@BeforeEach
	void setUp() {
		userSiteAccessEntityRepository.deleteAll();

		Site site = Site.builder()
			.name("name")
			.build();

		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		UUID communityId = UUID.fromString(communityRepository.create(community));

		Project project = Project.builder()
			.communityId(communityId.toString())
			.name("name")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();
		projectId = UUID.fromString(projectRepository.create(project));

		Site site1 = Site.builder()
			.name("name1")
			.build();

		siteId1 = UUID.fromString(siteRepository.create(site1, new SiteExternalId("id1")));

		Community community1 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		UUID communityId1 = UUID.fromString(communityRepository.create(community1));

		Project project1 = Project.builder()
			.communityId(communityId1.toString())
			.name("name1")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();
		projectId1 = UUID.fromString(projectRepository.create(project1));
	}

	@Test
	void shouldCreate(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId, projectId, "userId");
		UserSiteAccessEntity saved = userSiteAccessEntityRepository.save(userSiteAccessEntity);

		assertEquals(saved, userSiteAccessEntityRepository.findById(saved.getId()).get());
	}

	@Test
	void shouldDeleteById(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId, projectId, "userId");
		UserSiteAccessEntity saved = userSiteAccessEntityRepository.save(userSiteAccessEntity);

		userSiteAccessEntityRepository.deleteById(userSiteAccessEntity.getId());

		assertTrue(userSiteAccessEntityRepository.findById(saved.getId()).isEmpty());
	}

	@Test
	void shouldDeleteBySiteIdAndProjectIdAndUserId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId, projectId, "userId");
		UserSiteAccessEntity saved = userSiteAccessEntityRepository.save(userSiteAccessEntity);

		userSiteAccessEntityRepository.deleteBy(siteId, projectId, "userId");

		assertTrue(userSiteAccessEntityRepository.findById(saved.getId()).isEmpty());
	}

	@Test
	void shouldDeleteByProjectIdAndUserId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId, projectId, "userId");
		UserSiteAccessEntity saved = userSiteAccessEntityRepository.save(userSiteAccessEntity);
		UserSiteAccessEntity userSiteAccessEntity1 = new UserSiteAccessEntity(siteId1, projectId, "userId");
		UserSiteAccessEntity saved1 = userSiteAccessEntityRepository.save(userSiteAccessEntity1);

		userSiteAccessEntityRepository.deleteBy(projectId, "userId");

		assertTrue(userSiteAccessEntityRepository.findById(saved.getId()).isEmpty());
		assertTrue(userSiteAccessEntityRepository.findById(saved1.getId()).isEmpty());
	}

	@Test
	void shouldFindAllBySiteIdAndUserId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId, projectId, "userId");
		UserSiteAccessEntity saved = userSiteAccessEntityRepository.save(userSiteAccessEntity);

		UserSiteAccessEntity userSiteAccessEntity1 = new UserSiteAccessEntity(siteId, projectId1, "userId");
		UserSiteAccessEntity saved1 = userSiteAccessEntityRepository.save(userSiteAccessEntity1);

		UserSiteAccessEntity userSiteAccessEntity2 = new UserSiteAccessEntity(siteId1, projectId1, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity2);

		Set<UserSiteAccessEntity> entities = userSiteAccessEntityRepository.findAllBySiteIdAndUserId(siteId, "userId");

		assertEquals(Set.of(saved, saved1), entities);
	}

	@Test
	void shouldFindAllByProjectId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId, projectId, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity);

		UserSiteAccessEntity userSiteAccessEntity1 = new UserSiteAccessEntity(siteId, projectId1, "userId");
		UserSiteAccessEntity saved1 = userSiteAccessEntityRepository.save(userSiteAccessEntity1);

		UserSiteAccessEntity userSiteAccessEntity2 = new UserSiteAccessEntity(siteId1, projectId1, "userId");
		UserSiteAccessEntity saved2 = userSiteAccessEntityRepository.save(userSiteAccessEntity2);

		Set<UserSiteAccessEntity> entities = userSiteAccessEntityRepository.findAllByProjectId(projectId1);

		assertEquals(Set.of(saved1, saved2), entities);
	}

	@Test
	void shouldExistsBySiteIdAndProjectIdAndUserId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId, projectId, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity);

		UserSiteAccessEntity userSiteAccessEntity1 = new UserSiteAccessEntity(siteId, projectId1, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity1);

		UserSiteAccessEntity userSiteAccessEntity2 = new UserSiteAccessEntity(siteId1, projectId1, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity2);

		boolean exists = userSiteAccessEntityRepository.existsBySiteIdAndProjectIdAndUserId(siteId, projectId1, "userId");

		assertTrue(exists);
	}

	@Test
	void shouldNotExistsBySiteIdAndProjectIdAndUserId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId, projectId, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity);

		UserSiteAccessEntity userSiteAccessEntity1 = new UserSiteAccessEntity(siteId, projectId1, "userId1");
		userSiteAccessEntityRepository.save(userSiteAccessEntity1);

		UserSiteAccessEntity userSiteAccessEntity2 = new UserSiteAccessEntity(siteId1, projectId1, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity2);

		boolean exists = userSiteAccessEntityRepository.existsBySiteIdAndProjectIdAndUserId(siteId, projectId1, "userId");

		assertFalse(exists);
	}

	@Test
	void shouldNotDuplicateSiteIdAndProjectIdAndUserId(){
		UserSiteAccessEntity userSiteAccessEntity = new UserSiteAccessEntity(siteId, projectId, "userId");
		userSiteAccessEntityRepository.save(userSiteAccessEntity);

		assertThrows(
			DbActionExecutionException.class,
			() -> userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId, projectId, "userId"))
		);
	}
}
