/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */


CREATE TABLE audit_log (
    id UUID PRIMARY KEY NOT NULL,
    creation_time TIMESTAMP NOT NULL,
    originator_id VARCHAR(255),
    originator_persistent_id VARCHAR(255),
    operation_category INT NOT NULL,
    operation_action INT NOT NULL,
    operation_subject VARCHAR(255) NOT NULL,
    data_json TEXT
);

CREATE INDEX audit_log_creation_time_index ON audit_log (creation_time);
CREATE INDEX audit_log_originator_id_index ON audit_log (originator_id);
CREATE INDEX originator_persistent_id_index ON audit_log (originator_persistent_id);
CREATE INDEX audit_log_operation_action_id_index ON audit_log (operation_action);
CREATE INDEX audit_log_operation_category_id_index ON audit_log (operation_category);
CREATE INDEX audit_log_operation_subject_id_index ON audit_log (operation_subject);
