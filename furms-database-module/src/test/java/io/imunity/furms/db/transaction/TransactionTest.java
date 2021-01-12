/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.transaction;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;

@SpringBootTest
@TestInstance(PER_CLASS)
class TransactionTest {
	@Autowired
	private ServiceMock serviceMockImpl;
	@Autowired
	private CommunityRepository communityRepository;

	private byte[] imgTestFile;
	private byte[] imgTestFile2;

	@BeforeAll
	void init() throws IOException {
		imgTestFile = getClass().getClassLoader().getResourceAsStream("test.jpg").readAllBytes();
		imgTestFile2 = getClass().getClassLoader().getResourceAsStream("test2.jpg").readAllBytes();
	}

	@Test
	void shouldRollbackCommunity() {
		//given
		String id = communityRepository.create(Community.builder()
			.name("test")
			.description("test")
			.logo(imgTestFile, "jpg")
			.build());
		Community secondCommunity = Community.builder()
			.id(id)
			.name("test2")
			.description("test2")
			.logo(imgTestFile2, "jpg")
			.build();

		//when
		assertThrows(RuntimeException.class, () -> serviceMockImpl.update(secondCommunity));

		//then
		Optional<Community> firstCommunity = communityRepository.findById(id);
		assertThat(firstCommunity).isPresent();
		assertThat(firstCommunity.get().getName()).isEqualTo("test");
		assertThat(firstCommunity.get().getDescription()).isEqualTo("test");
		assertThat(firstCommunity.get().getLogo()).isEqualTo(new FurmsImage(imgTestFile, "jpg"));
	}
}
