/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteServiceValidatorTest {

	@Mock
	private SiteRepository siteRepository;

	@InjectMocks
	private SiteServiceValidator validator;

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
		when(siteRepository.isUniqueName(site.getId(), site.getName())).thenReturn(true);

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
		when(siteRepository.isUniqueName(site.getId(), site.getName())).thenReturn(false);

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

	@Test
	void shouldPassForUniqueCombinationIdAndName() {
		//given
		final Site site = Site.builder()
				.id("id")
				.name("name")
				.build();

		when(siteRepository.isUniqueName(site.getId(), site.getName())).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateUniqueName(site.getId(), site.getName()));
	}

	@Test
	void shouldNotPassForNonUniqueCombinationIdAndName() {
		//given
		final Site site = Site.builder()
				.id("id")
				.name("name")
				.build();

		when(siteRepository.isUniqueName(site.getId(), site.getName())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUniqueName(site.getId(), site.getName()));
	}

}