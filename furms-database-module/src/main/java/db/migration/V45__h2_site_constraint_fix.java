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

public class V45__h2_site_constraint_fix extends BaseJavaMigration {

	public void migrate(Context context) throws SQLException {
		Connection connection = context.getConnection();

		JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
		String databaseProductName = connection.getMetaData().getDatabaseProductName();
		if(databaseProductName.equals("H2"))
			migrate(jdbcTemplate);
	}

	private static void migrate(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.execute("ALTER TABLE site DROP CONSTRAINT IF EXISTS CONSTRAINT_35DF;");
		jdbcTemplate.execute("ALTER TABLE site ADD CONSTRAINT CONSTRAINT_35DF FOREIGN KEY (policy_id) REFERENCES policy_document(id) ON DELETE CASCADE;");
	}
}