/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

ALTER TABLE site ADD COLUMN short_id VARCHAR(5) UNIQUE NOT NULL;