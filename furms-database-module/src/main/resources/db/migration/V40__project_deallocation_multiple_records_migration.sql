/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

/*
	Create view that represent all Project Deallocations that are not in FAILED status.
    Grouped by project_allocation_id and status to count duplications.
 */
CREATE OR REPLACE VIEW view_deallocations_ack_pendings_by_allocation_id AS
SELECT count(id) as count, project_allocation_id, status
FROM project_deallocation pd
WHERE status != 2
GROUP BY project_allocation_id, status;

/*
    Create views that represent all Project Deallocations that are represents
    Project Allocations which are in ACK and PENDING status at same time.
 */
CREATE OR REPLACE VIEW view_deallocations_with_both_ack_pending_statuses AS
SELECT project_allocation_id
FROM (SELECT count(project_allocation_id) as count, project_allocation_id
      FROM view_deallocations_ack_pendings_by_allocation_id
      GROUP BY project_allocation_id) tmp_view
WHERE tmp_view.count > 1;

/*
    Create views that represent all Project Deallocations that are ONLY
    in PENDING status and have illegal multiplications
 */
CREATE OR REPLACE VIEW view_deallocations_multiple_with_only_pending_status AS
SELECT project_allocation_id
FROM view_deallocations_ack_pendings_by_allocation_id tmp_view
WHERE tmp_view.count > 1
  AND tmp_view.status = 0
  AND tmp_view.project_allocation_id NOT IN
      (SELECT project_allocation_id FROM view_deallocations_with_both_ack_pending_statuses);

/*
    REMOVE all PENDING Project Allocations that are in ACK status also.
 */
DELETE
FROM project_deallocation pd
WHERE pd.status = 0
  AND project_allocation_id IN (SELECT project_allocation_id FROM view_deallocations_with_both_ack_pending_statuses);

/*
    REMOVE all multiplied PENDING Project Allocations (leave random one)
 */
DELETE
FROM project_deallocation
WHERE id IN (SELECT pd.id
             FROM project_deallocation pd
	                  JOIN (SELECT pdin.*
	                        FROM project_deallocation pdin
		                             JOIN view_deallocations_multiple_with_only_pending_status tmp_view
		                                  ON tmp_view.project_allocation_id = pdin.project_allocation_id
	                        ORDER BY pdin.id
	                        LIMIT 1)
	             AS allocations_to_stay
	                       ON allocations_to_stay.id != pd.id
		                       AND allocations_to_stay.project_allocation_id = pd.project_allocation_id);

/*
    REMOVE VIEWS AND CLEAN UP AFTER MIGRATION
 */
DROP VIEW view_deallocations_multiple_with_only_pending_status;
DROP VIEW view_deallocations_with_both_ack_pending_statuses;
DROP VIEW view_deallocations_ack_pendings_by_allocation_id;