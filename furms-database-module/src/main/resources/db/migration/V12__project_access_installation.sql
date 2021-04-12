/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE project_installation_job (
    id UUID PRIMARY KEY NOT NULL,
    correlation_Id UUID NOT NULL,
    status VARCHAR(255) NOT NULL
);