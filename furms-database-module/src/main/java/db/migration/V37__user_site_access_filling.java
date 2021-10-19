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

public class V37__user_site_access_filling extends BaseJavaMigration {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public void migrate(Context context) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
			new SingleConnectionDataSource(context.getConnection(), true);
		migrate(jdbcTemplate);
	}

	static void migrate(JdbcTemplate jdbcTemplate) {
		List<ProjectSiteUserId> siteAndProjectAndUserIds = jdbcTemplate.query(
			"SELECT ug.site_id, ug.project_id, ug.user_id " +
				"FROM user_grant ug",
			(rs, rowNum) -> new ProjectSiteUserId(
				UUID.fromString(rs.getString("site_id")),
				UUID.fromString(rs.getString("project_id")),
				rs.getString("user_id"))
		);

		siteAndProjectAndUserIds.stream()
			.distinct()
			.peek(jobEntity -> LOG.info("This user site access will be created: {}", jobEntity))
			.forEach(
				projectSiteUserId ->
					jdbcTemplate.update(
						"INSERT INTO user_site_access (id, site_id, project_id, user_id) VALUES (?, ?, ?, ?)",
						new Object[] {UUID.randomUUID(), projectSiteUserId.siteId, projectSiteUserId.projectId, projectSiteUserId.userId})
			);
	}
}