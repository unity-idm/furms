/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

ALTER TABLE site ADD COLUMN connection_info VARCHAR(510);
ALTER TABLE site ADD COLUMN logo BYTEA;
ALTER TABLE site ADD COLUMN logo_type VARCHAR(255);