/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE resource_usage (
    id UUID PRIMARY KEY NOT NULL,
    site_id UUID NOT NULL,
    community_id UUID NOT NULL,
    resource_credit_id UUID NOT NULL,
    project_id UUID NOT NULL,
    project_allocation_id UUID NOT NULL,
    cumulative_consumption DECIMAL,
    probed_at TIMESTAMP,
    CONSTRAINT resource_usage_project_allocation_id_unique UNIQUE (project_id, project_allocation_id),
    FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE,
    FOREIGN KEY (community_id) REFERENCES community(id) ON DELETE CASCADE,
    FOREIGN KEY (resource_credit_id) REFERENCES resource_credit(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (project_allocation_id) REFERENCES project_allocation(id) ON DELETE CASCADE
);

CREATE TABLE resource_usage_history (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    site_id UUID NOT NULL,
    community_id UUID NOT NULL,
    resource_credit_id UUID NOT NULL,
    project_id UUID NOT NULL,
    project_allocation_id UUID NOT NULL,
    cumulative_consumption DECIMAL,
    probed_at TIMESTAMP,
    FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE,
    FOREIGN KEY (community_id) REFERENCES community(id) ON DELETE CASCADE,
    FOREIGN KEY (resource_credit_id) REFERENCES resource_credit(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (project_allocation_id) REFERENCES project_allocation(id) ON DELETE CASCADE
);

CREATE TABLE user_resource_usage (
    id UUID PRIMARY KEY NOT NULL,
    project_id UUID NOT NULL,
    project_allocation_id UUID NOT NULL,
    fenix_user_id VARCHAR(255) NOT NULL,
    cumulative_consumption DECIMAL,
    consumed_until TIMESTAMP,
    CONSTRAINT user_resource_usage_project_allocation_id_and_fenix_user_id_unique UNIQUE (project_id, project_allocation_id, fenix_user_id),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (project_allocation_id) REFERENCES project_allocation(id) ON DELETE CASCADE
);

CREATE TABLE user_resource_usage_history (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    project_id UUID NOT NULL,
    project_allocation_id UUID NOT NULL,
    fenix_user_id VARCHAR(255) NOT NULL,
    cumulative_consumption DECIMAL,
    consumed_until TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (project_allocation_id) REFERENCES project_allocation(id) ON DELETE CASCADE
);