/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.db.ssh_key;

import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.repository.CrudRepository;

public interface SSHKeyEntityRepository extends CrudRepository<SSHKeyEntity, UUID> {
	
	Stream<SSHKeyEntity> findAllByOwnerId(String id);
	
	boolean existsByName(String name);

	boolean existsByNameAndIdIsNot(String name, UUID id);
}
