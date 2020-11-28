/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteServiceAPITest {

    @Mock
    private SiteRepository repository;
    private SiteServiceAPIValidator validator;
    private SiteServiceAPI service;

    @BeforeEach
    void setUp() {
        validator = new SiteServiceAPIValidator(repository);
        service = new SiteServiceAPI(repository, validator);
    }

    @Test
    void shouldReturnSiteIfExistsInRepository() {
        //given
        final String id = "id";
        when(repository.findById(id)).thenReturn(Optional.of(Site.builder()
                .id(id)
                .name("name")
                .build()));

        //when
        final Optional<Site> byId = service.findById(id);
        final Optional<Site> otherId = service.findById("otherId");

        //then
        assertThat(byId).isPresent();
        assertThat(byId.get().getId()).isEqualTo(id);
        assertThat(otherId).isEmpty();
    }

    @Test
    void shouldReturnAllSitesIfExistsInRepository() {
        //given
        when(repository.findAll()).thenReturn(Set.of(
                Site.builder().id("id1").name("name").build(),
                Site.builder().id("id2").name("name").build()));

        //when
        final Set<Site> allSites = service.findAll();

        //then
        assertThat(allSites).hasSize(2);
    }

    @Test
    void shouldAllowToCreateSite() {
        //given
        final Site request = Site.builder()
                .name("name")
                .build();
        when(repository.isUniqueName(request.getName())).thenReturn(true);

        //when
        service.create(request);
    }

    @Test
    void shouldNotAllowToCreateSiteDueToNonUniqueName() {
        //given
        final Site request = Site.builder()
                .name("name")
                .build();
        when(repository.isUniqueName(request.getName())).thenReturn(false);

        //when
        assertThrows(IllegalArgumentException.class, () -> service.create(request));
    }

    @Test
    void shouldAllowToUpdateSite() {
        //given
        final Site request = Site.builder()
                .id("id")
                .name("name")
                .build();
        when(repository.exists(request.getId())).thenReturn(true);
        when(repository.isUniqueName(request.getName())).thenReturn(true);

        //when
        service.update(request);
    }

    @Test
    void shouldAllowToDeleteSite() {
        //given
        final String id = "id";
        when(repository.exists(id)).thenReturn(true);

        //when
        service.delete(id);
    }

    @Test
    void shouldNotAllowToDeleteSiteDueToSiteNotExists() {
        //given
        final String id = "id";
        when(repository.exists(id)).thenReturn(false);

        //when
        assertThrows(IllegalArgumentException.class, () -> service.delete(id));
    }

}