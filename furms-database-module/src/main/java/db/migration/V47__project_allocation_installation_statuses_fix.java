/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class V47__project_allocation_installation_statuses_fix extends BaseJavaMigration {

	public void migrate(Context context) throws SQLException {
		Connection connection = context.getConnection();

		JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
		migrate(jdbcTemplate);
	}

	static void migrate(JdbcTemplate jdbcTemplate) {
		List<UUID> idToUpdate = jdbcTemplate.query(
			"SELECT DISTINCT pai.id " +
				"FROM project_allocation_installation pai " +
				"JOIN project_allocation_chunk pac ON pai.project_allocation_id = pac.project_allocation_id " +
				"WHERE pai.status = 2",
			(rs, rowNum) ->
				UUID.fromString(rs.getString("id"))
		);

		idToUpdate
			.forEach(id -> jdbcTemplate.update("UPDATE project_allocation_installation SET status = 7 WHERE id = ?",
				new Object[] {id}));
	}
}