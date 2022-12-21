/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;

import java.util.Objects;

class UserAdditionReadWithProjectsEntity extends UUIDIdentifiable {

	public final String siteName;
	public final String projectId;
	public final String projectName;
	public final String uid;
	public final String gid;
	public final int status;
	public final String code;
	public final String message;

	public UserAdditionReadWithProjectsEntity(String siteName,
	                                          String projectId,
	                                          String projectName,
	                                          String uid,
	                                          String gid,
	                                          int status,
	                                          String code,
	                                          String message) {
		this.siteName = siteName;
		this.projectId = projectId;
		this.projectName = projectName;
		this.uid = uid;
		this.gid = gid;
		this.status = status;
		this.code = code;
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAdditionReadWithProjectsEntity that = (UserAdditionReadWithProjectsEntity) o;
		return status == that.status &&
				Objects.equals(siteName, that.siteName) &&
				Objects.equals(projectId, that.projectId) &&
				Objects.equals(projectName, that.projectName) &&
				Objects.equals(uid, that.uid) &&
				Objects.equals(gid, that.gid) &&
				Objects.equals(code, that.code) &&
				Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteName, projectId, projectName, uid, gid, status, code, message);
	}

	@Override
	public String toString() {
		return "UserAdditionReadWithProjectsEntity{" +
				"siteName='" + siteName + '\'' +
				", projectId='" + projectId + '\'' +
				", projectName='" + projectName + '\'' +
				", uid='" + uid + '\'' +
				", gid='" + gid + '\'' +
				", status=" + status +
				", code=" + code +
				", message='" + message + '\'' +
				'}';
	}
}
