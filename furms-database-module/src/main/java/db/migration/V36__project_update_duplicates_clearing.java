/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.UUID;

public class V36__project_update_duplicates_clearing extends BaseJavaMigration {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public void migrate(Context context) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
			new SingleConnectionDataSource(context.getConnection(), true);
		migrate(jdbcTemplate);
	}

	static void migrate(JdbcTemplate jdbcTemplate) {
		List<ProjectSiteId> duplicatedIds = jdbcTemplate.query(
			"SELECT pij.site_Id, pij.project_Id " +
				"FROM project_update_job pij " +
				"GROUP BY pij.site_Id, pij.project_Id " +
				"HAVING COUNT(*) > 1",
			(rs, rowNum) -> new ProjectSiteId(
				UUID.fromString(rs.getString("site_id")),
				UUID.fromString(rs.getString("project_id")))
		);

		duplicatedIds.stream()
			.peek(projectSiteId -> LOG.warn("All updates with this ids {} will be removed", projectSiteId))
			.forEach(projectSiteId -> jdbcTemplate.update("DELETE FROM project_update_job WHERE site_id = ? AND project_id = ?", new Object[] {projectSiteId.siteId, projectSiteId.projectId}));
	}
}