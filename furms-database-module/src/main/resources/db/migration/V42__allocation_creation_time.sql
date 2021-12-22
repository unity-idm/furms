/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

ALTER TABLE project_allocation ADD COLUMN creation_time TIMESTAMP;
ALTER TABLE community_allocation ADD COLUMN creation_time TIMESTAMP;