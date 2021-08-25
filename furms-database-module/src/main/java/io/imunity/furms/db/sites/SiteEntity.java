/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Table("site")
public class SiteEntity extends UUIDIdentifiable {

	private final String name;
	private final String oauthClientId;
	private final String connectionInfo;
	private final byte[] logo;
	private final String logoType;
	private final Boolean sshKeyFromOptionMandatory;
	private final Integer sshKeyHistoryLength;
	private final String externalId;
	private final UUID policyId;


	SiteEntity(UUID id, String name, String oauthClientId, String connectionInfo, byte[] logo, String logoType,
	           Boolean sshKeyFromOptionMandatory, Integer sshKeyHistoryLength, String externalId, UUID policyId) {
		this.id = id;
		this.name = name;
		this.oauthClientId = oauthClientId;
		this.connectionInfo = connectionInfo;
		this.logo = logo;
		this.logoType = logoType;
		this.sshKeyFromOptionMandatory = sshKeyFromOptionMandatory;
		this.sshKeyHistoryLength = sshKeyHistoryLength;
		this.externalId = externalId;
		this.policyId = policyId;
	}

	public Site toSite() {
		return Site.builder()
				.id(id.toString())
				.name(name)
				.oauthClientId(oauthClientId)
				.connectionInfo(connectionInfo)
				.logo(new FurmsImage(logo, logoType))
				.sshKeyFromOptionMandatory(sshKeyFromOptionMandatory)
				.sshKeyHistoryLength(sshKeyHistoryLength)
				.externalId(new SiteExternalId(externalId))
				.policyId(new PolicyId(policyId))
				.build();
	}

	public String getName() {
		return name;
	}

	public String getExternalId() {
		return externalId;
	}

	public UUID getPolicyId() {
		return policyId;
	}

	public static SiteEntity.SiteEntityBuilder builder() {
		return new SiteEntity.SiteEntityBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteEntity entity = (SiteEntity) o;
		return Objects.equals(id, entity.id) &&
				Objects.equals(name, entity.name) &&
				Objects.equals(oauthClientId, entity.oauthClientId) &&
				Objects.equals(connectionInfo, entity.connectionInfo) &&
				Arrays.equals(logo, entity.logo) &&
				Objects.equals(logoType, entity.logoType)&&
				Objects.equals(sshKeyFromOptionMandatory, entity.sshKeyFromOptionMandatory) &&
				Objects.equals(sshKeyHistoryLength, entity.sshKeyHistoryLength) &&
				Objects.equals(externalId, entity.externalId) &&
				Objects.equals(policyId, entity.policyId);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(id, name, oauthClientId, connectionInfo, logoType, sshKeyFromOptionMandatory,
				externalId, sshKeyHistoryLength, policyId);
		result = 31 * result + Arrays.hashCode(logo);
		return result;
	}

	@Override
	public String toString() {
		return "SiteEntity{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", oauthClientId='" + oauthClientId + '\'' +
				", logo=" + Arrays.toString(logo) +
				", logoType='" + logoType + '\'' +
				", sshKeyFromOptionMandatory=" + sshKeyFromOptionMandatory +
				", sshKeyHistoryLength=" + sshKeyHistoryLength +
				", externalId=" + externalId +
				", policyId=" + policyId +
				'}';
	}

	public static class SiteEntityBuilder {

		private UUID id;
		private String name;
		private String oauthClientId;
		private String connectionInfo;
		private byte[] logo;
		private String logoType;
		private Boolean sshKeyFromOptionMandatory;
		private String externalId;
		private Integer sshKeyHistoryLength;
		private UUID policyId;

		public SiteEntity.SiteEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public SiteEntity.SiteEntityBuilder policyId(UUID policyId) {
			this.policyId = policyId;
			return this;
		}

		public SiteEntity.SiteEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public SiteEntity.SiteEntityBuilder externalId(String externalId) {
			this.externalId = externalId;
			return this;
		}

		public SiteEntityBuilder oauthClientId(String oauthClientId) {
			this.oauthClientId = oauthClientId;
			return this;
		}

		public SiteEntity.SiteEntityBuilder connectionInfo(String connectionInfo) {
			this.connectionInfo = connectionInfo;
			return this;
		}

		public SiteEntity.SiteEntityBuilder logo(FurmsImage logo) {
			if (logo != null) {
				this.logo = logo.getImage();
				this.logoType = logo.getType();
			}
			return this;
		}

		public SiteEntity.SiteEntityBuilder sshKeyFromOptionMandatory(Boolean sshKeyFromMandatory) {
			this.sshKeyFromOptionMandatory = sshKeyFromMandatory;
			return this;
		}

		public SiteEntity.SiteEntityBuilder sshKeyHistoryLength(Integer sshKeyHistoryLength) {
			this.sshKeyHistoryLength = sshKeyHistoryLength;
			return this;
		}


		public SiteEntity build() {
			return new SiteEntity(id, name, oauthClientId, connectionInfo, logo, logoType, sshKeyFromOptionMandatory,
					sshKeyHistoryLength, externalId, policyId);
		}
	}
}
