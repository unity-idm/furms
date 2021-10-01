/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE generic_group (
    id UUID PRIMARY KEY NOT NULL,
    community_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(510),
    FOREIGN KEY (community_id) REFERENCES community(id) ON DELETE CASCADE
);

CREATE TABLE generic_group_membership (
    id UUID PRIMARY KEY NOT NULL,
    generic_group_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    member_since TIMESTAMP NOT NULL,
    CONSTRAINT user_id_generic_group_id_unique UNIQUE (generic_group_id, user_id),
    FOREIGN KEY (generic_group_id) REFERENCES generic_group(id) ON DELETE CASCADE
);
