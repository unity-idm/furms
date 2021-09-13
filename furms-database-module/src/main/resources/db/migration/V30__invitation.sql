/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE invitation (
    id UUID PRIMARY KEY NOT NULL,
    resource_id UUID,
    user_id VARCHAR(255),
    resource_name VARCHAR(255),
    originator VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    role_attribute VARCHAR(255) NOT NULL,
    role_value VARCHAR(255) NOT NULL,
    resource_type INT NOT NULL,
    code VARCHAR(255),
    expired_at TIMESTAMP NOT NULL,
    CONSTRAINT user_id_resource_id_invitation_unique UNIQUE (resource_id, user_id, role_value),
    CONSTRAINT user_id_code_invitation_unique UNIQUE (user_id, code),
    CONSTRAINT invitation_consistency CHECK (user_id IS NOT NULL OR code IS NOT NULL)
);
