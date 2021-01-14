/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SiteServiceValidatorTest {

	@Mock
	private SiteRepository siteRepository;

	@InjectMocks
	private SiteServiceValidator validator;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void shouldPassCreateForUniqueName() {
		//given
		final Site site = Site.builder()
				.name("name")
				.build();

		when(siteRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(site));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		final Site site = Site.builder()
				.name("name")
				.build();

		when(siteRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(site));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		final Site site = Site.builder()
				.id("id")
				.name("name")
				.build();

		when(siteRepository.exists(site.getId())).thenReturn(true);
		when(siteRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(site));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		final Site site = Site.builder()
				.id("id")
				.name("name")
				.build();

		when(siteRepository.exists(site.getId())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(site));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		final Site site = Site.builder()
				.id("id")
				.name("name")
				.build();

		when(siteRepository.exists(site.getId())).thenReturn(true);
		when(siteRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(site));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		final String id = "id";

		when(siteRepository.exists(id)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		final String id = "id";

		when(siteRepository.exists(id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

}