/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.sites;

import java.util.Optional;
import java.util.Set;

public interface SiteRepository<T, ID> {

    Optional<T> findOneById(ID id);

    Set<T> finAll();

    T save(T site);

    boolean exists(ID siteId);

    boolean isUniqueName(String name);

    void delete(ID siteId);
}
