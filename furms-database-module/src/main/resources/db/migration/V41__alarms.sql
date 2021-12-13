/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE alarm_configuration (
    id UUID PRIMARY KEY NOT NULL,
    project_id UUID NOT NULL,
    project_allocation_id UUID UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    all_users BOOLEAN NOT NULL,
    fired BOOLEAN NOT NULL,
    threshold INT CONSTRAINT alarm_threshold_range CHECK (threshold BETWEEN  1 AND 100),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (project_allocation_id) REFERENCES project_allocation(id) ON DELETE CASCADE
);

CREATE TABLE alarm_configuration_user (
    id UUID PRIMARY KEY NOT NULL,
    alarm_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (alarm_id) REFERENCES alarm_configuration(id) ON DELETE CASCADE
);
