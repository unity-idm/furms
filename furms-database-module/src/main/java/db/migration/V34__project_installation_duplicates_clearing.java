/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package db.migration;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.db.project_installation.ProjectInstallationJobEntity;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class V34__project_installation_duplicates_clearing extends BaseJavaMigration {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public void migrate(Context context) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
			new SingleConnectionDataSource(context.getConnection(), true);
		migrate(jdbcTemplate);
	}

	static void migrate(JdbcTemplate jdbcTemplate) {
		List<ProjectSiteId> duplicatedIds = jdbcTemplate.query(
			"SELECT pij.site_Id, pij.project_Id " +
				"FROM project_installation_job pij " +
				"GROUP BY pij.site_Id, pij.project_Id " +
				"HAVING COUNT(*) > 1",
			(rs, rowNum) -> new ProjectSiteId(
				UUID.fromString(rs.getString("site_id")),
				UUID.fromString(rs.getString("project_id")))
		);

		Set<ProjectInstallationJobEntity> duplicatedInstallations = duplicatedIds.stream()
			.map(projectSiteId ->
				jdbcTemplate.query(
					"SELECT pij.* " +
						"FROM project_installation_job pij " +
						"WHERE pij.site_id = ? AND pij.project_id = ?",
					new Object[] {projectSiteId.siteId, projectSiteId.projectId},
					(rs, rowNum) -> ProjectInstallationJobEntity.builder()
						.id(UUID.fromString(rs.getString("id")))
						.correlationId(UUID.fromString(rs.getString("correlation_id")))
						.siteId(UUID.fromString(rs.getString("site_id")))
						.projectId(UUID.fromString(rs.getString("project_id")))
						.status(rs.getInt("status"))
						.gid(rs.getString("gid"))
						.code(rs.getString("code"))
						.message(rs.getString("message"))
						.build()
				)
			)
			.flatMap(Collection::stream)
			.peek(x -> LOG.warn("Duplicate project installation: {}", x))
			.collect(Collectors.toSet());

		Collection<ProjectInstallationJobEntity> toNotRemoveInstallation = duplicatedInstallations.stream()
			.collect(Collectors.toMap(
				jobEntity -> Pair.of(jobEntity.siteId, jobEntity.projectId),
				Function.identity(),
				BinaryOperator.maxBy(Comparator.comparing(jobEntity -> jobEntity.status)))
			).values();

		duplicatedInstallations.removeAll(toNotRemoveInstallation);

		duplicatedInstallations.stream()
			.peek(jobEntity -> LOG.warn("This installation will be removed: {}", jobEntity))
			.map(UUIDIdentifiable::getId)
			.forEach(id -> jdbcTemplate.update("DELETE FROM project_installation_job WHERE id = ?", new Object[] {id}));
	}
}