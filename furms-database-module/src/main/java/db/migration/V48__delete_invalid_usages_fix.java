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

public class V48__delete_invalid_usages_fix extends BaseJavaMigration {

	public void migrate(Context context) throws SQLException {
		Connection connection = context.getConnection();

		JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
		migrate(jdbcTemplate);
	}

	static void migrate(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.execute(
		"DELETE FROM resource_usage WHERE cumulative_consumption IS NULL;"
		);
		jdbcTemplate.execute(
			"DELETE FROM resource_usage_history WHERE cumulative_consumption IS NULL;"
		);
		jdbcTemplate.execute(
			"DELETE FROM user_resource_usage WHERE cumulative_consumption IS NULL;"
		);
		jdbcTemplate.execute(
			"DELETE FROM user_resource_usage_history WHERE cumulative_consumption IS NULL;"
		);
	}
}