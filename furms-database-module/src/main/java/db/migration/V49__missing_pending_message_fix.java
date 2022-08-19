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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.INSTALLATION_ACKNOWLEDGED;

public class V49__missing_pending_message_fix extends BaseJavaMigration {

	private final static String jsonTemplate = "{\"header\":{\"version\":\"1\",\"messageCorrelationId\":\"%s\"," +
		"\"status\":\"OK\"},\"body\":{\"ProjectResourceAllocationRequest\":{\"projectIdentifier\":\"%s\"," +
		"\"allocationIdentifier\":\"%s\",\"resourceCreditIdentifier\":\"%s\"," +
		"\"resourceType\":\"%s\",\"amount\":%s,\"validFrom\":\"%s\"," +
		"\"validTo\":\"%s\"}}}";

	public void migrate(Context context) throws SQLException {
		Connection connection = context.getConnection();

		JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
		migrate(jdbcTemplate);
	}
	static void migrate(JdbcTemplate jdbcTemplate) {

		List<DataHolder> data = jdbcTemplate.query(
			"select " +
				"s.id as site_id, s.external_id as site_external_id, " +
				"rt.name as resourceType, " +
				"rc.id as resourceCredit_id, rc.start_time as validFrom, rc.end_time as validTo, " +
				"pai.correlation_id as correlation_id, " +
				"a.amount as amount, a.id as allocation_id, a.creation_time as creation, a.project_id as project_id " +
				"from project_allocation a " +
				"join project_allocation_installation pai on a.id = pai.project_allocation_id " +
				"join community_allocation ca on a.community_allocation_id = ca.id " +
				"join resource_credit rc on ca.resource_credit_id = rc.id " +
				"join site s on rc.site_id = s.id " +
				"join resource_type rt on rc.resource_type_id = rt.id " +
				"where pai.status = " + INSTALLATION_ACKNOWLEDGED.getPersistentId(),
			(rs, rowNum) -> new DataHolder(
				UUID.fromString(rs.getString("site_id")),
				rs.getString("site_external_id"),
				UUID.fromString(rs.getString("correlation_id")),
				rs.getString("allocation_id"),
				rs.getString("resourceCredit_id"),
				rs.getString("project_id"),
				rs.getString("resourceType"),
				rs.getString("amount"),
				rs.getTimestamp("validFrom").toLocalDateTime().atOffset(ZoneOffset.UTC).toString(),
				rs.getTimestamp("validTo").toLocalDateTime().atOffset(ZoneOffset.UTC).toString(),
				rs.getTimestamp("creation").toLocalDateTime()
			)
		);

		data
			.forEach(row -> jdbcTemplate.update("INSERT INTO site_agent_pending_message VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
				new Object[] {
					UUID.randomUUID(),
					row.siteId,
					row.siteExternalId,
					row.correlationId,
					0,
					crateJson(row),
					row.creation,
					LocalDateTime.now()
				}));
	}

	private static String crateJson(DataHolder id) {
		return String.format(jsonTemplate, id.correlationId, id.project_id, id.allocationId, id.resourceCreditId,
			id.resourceType, id.amount, id.validFrom, id.validTo);
	}

	static class DataHolder {
		public final UUID siteId;
		public final String siteExternalId;
		public final UUID correlationId;
		public final String allocationId;
		public final String resourceCreditId;
		public final String project_id;
		public final String resourceType;
		public final String amount;
		public final String validFrom;
		public final String validTo;
		public final LocalDateTime creation;

		DataHolder(UUID siteId, String siteExternalId, UUID correlationId, String allocationId,
		           String resourceCreditId, String project_id, String resourceType, String amount, String validFrom,
		           String validTo, LocalDateTime creation) {
			this.siteId = siteId;
			this.siteExternalId = siteExternalId;
			this.correlationId = correlationId;
			this.allocationId = allocationId;
			this.resourceCreditId = resourceCreditId;
			this.project_id = project_id;
			this.resourceType = resourceType;
			this.amount = amount;
			this.validFrom = validFrom;
			this.validTo = validTo;
			this.creation = creation;
		}

		@Override
		public String toString() {
			return "DataHolder{" +
				"siteId='" + siteId + '\'' +
				", siteExternalId='" + siteExternalId + '\'' +
				", correlationId='" + correlationId + '\'' +
				", allocationId='" + allocationId + '\'' +
				", resourceCreditId='" + resourceCreditId + '\'' +
				", project_id='" + project_id + '\'' +
				", resourceType='" + resourceType + '\'' +
				", amount='" + amount + '\'' +
				", validFrom='" + validFrom + '\'' +
				", validTo='" + validTo + '\'' +
				", creation='" + creation + '\'' +
				'}';
		}
	}
}