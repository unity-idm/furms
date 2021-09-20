/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.db.ssh_keys;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

interface SSHKeyEntityRepository extends CrudRepository<SSHKeyEntity, UUID> {
	
	Set<SSHKeyEntity> findAllByOwnerId(String id);

	boolean existsByName(String name);

	boolean existsByNameAndIdIsNot(String name, UUID id);
}
