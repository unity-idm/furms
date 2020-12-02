/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.spi.communites.CommunityRepository;
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
class CommunityServiceImpTest {

    @Mock
    private CommunityRepository repository;
    private CommunityServiceValidator validator;
    private CommunityServiceImp service;

    @BeforeEach
    void setUp() {
        validator = new CommunityServiceValidator(repository);
        service = new CommunityServiceImp(repository, validator);
    }

    @Test
    void shouldReturnCommunityIfExistsInRepository() {
        //given
        String id = "id";
        when(repository.findById(id)).thenReturn(Optional.of(Community.builder()
                .id(id)
                .userFacingName("userFacingName")
                .build()));

        //when
        Optional<Community> byId = service.findById(id);
        Optional<Community> otherId = service.findById("otherId");

        //then
        assertThat(byId).isPresent();
        assertThat(byId.get().getId()).isEqualTo(id);
        assertThat(otherId).isEmpty();
    }

    @Test
    void shouldReturnAllCommunitysIfExistsInRepository() {
        //given
        when(repository.findAll()).thenReturn(Set.of(
                Community.builder().id("id1").userFacingName("userFacingName").build(),
                Community.builder().id("id2").userFacingName("userFacingName").build()));

        //when
        final Set<Community> allCommunitys = service.findAll();

        //then
        assertThat(allCommunitys).hasSize(2);
    }

    @Test
    void shouldAllowToCreateCommunity() {
        //given
        final Community request = Community.builder()
                .userFacingName("userFacingName")
                .build();
        when(repository.isUniqueUserFacingName(request.getUserFacingName())).thenReturn(true);

        //when
        service.create(request);
    }

    @Test
    void shouldNotAllowToCreateCommunityDueToNonUniqueuserFacingName() {
        //given
        final Community request = Community.builder()
                .userFacingName("userFacingName")
                .build();
        when(repository.isUniqueUserFacingName(request.getUserFacingName())).thenReturn(false);

        //when
        assertThrows(IllegalArgumentException.class, () -> service.create(request));
    }

    @Test
    void shouldAllowToUpdateCommunity() {
        //given
        final Community request = Community.builder()
                .id("id")
                .userFacingName("userFacingName")
                .build();
        when(repository.exists(request.getId())).thenReturn(true);
        when(repository.isUniqueUserFacingName(request.getUserFacingName())).thenReturn(true);

        //when
        service.update(request);
    }

    @Test
    void shouldAllowToDeleteCommunity() {
        //given
        final String id = "id";
        when(repository.exists(id)).thenReturn(true);

        //when
        service.delete(id);
    }

    @Test
    void shouldNotAllowToDeleteCommunityDueToCommunityNotExists() {
        //given
        final String id = "id";
        when(repository.exists(id)).thenReturn(false);

        //when
        assertThrows(IllegalArgumentException.class, () -> service.delete(id));
    }

}