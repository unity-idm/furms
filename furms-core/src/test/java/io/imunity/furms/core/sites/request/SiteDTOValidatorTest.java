/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites.request;

import io.imunity.furms.api.sites.web.SiteRequestDTO;
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

class SiteDTOValidatorTest {

    @Mock
    private SiteRepository<Site, Long> siteRepository;

    @InjectMocks
    private SiteDTOValidator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldPassCreateForUniqueName() {
        //given
        final SiteRequestDTO request = new SiteRequestDTO("name");

        when(siteRepository.isUniqueName(any())).thenReturn(true);

        //when+then
        assertDoesNotThrow(() -> validator.validateCreate(request));
    }

    @Test
    void shouldNotPassCreateForNonUniqueName() {
        //given
        final SiteRequestDTO request = new SiteRequestDTO("name");

        when(siteRepository.isUniqueName(any())).thenReturn(false);

        //when+then
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(request));
    }

    @Test
    void shouldPassUpdateForUniqueName() {
        //given
        final Long siteId = 1L;
        final SiteRequestDTO request = new SiteRequestDTO("name");

        when(siteRepository.exists(siteId)).thenReturn(true);
        when(siteRepository.isUniqueName(any())).thenReturn(true);

        //when+then
        assertDoesNotThrow(() -> validator.validateUpdate(siteId, request));
    }

    @Test
    void shouldNotPassUpdateForNonExistingObject() {
        //given
        final Long siteId = 1L;
        final SiteRequestDTO request = new SiteRequestDTO("name");

        when(siteRepository.exists(siteId)).thenReturn(false);

        //when+then
        assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(siteId, request));
    }

    @Test
    void shouldNotPassUpdateForNonUniqueName() {
        //given
        final Long siteId = 1L;
        final SiteRequestDTO request = new SiteRequestDTO("name");

        when(siteRepository.exists(siteId)).thenReturn(true);
        when(siteRepository.isUniqueName(any())).thenReturn(false);

        //when+then
        assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(siteId, request));
    }

}