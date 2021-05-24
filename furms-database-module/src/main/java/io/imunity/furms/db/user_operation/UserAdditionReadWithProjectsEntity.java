/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;

import java.util.Objects;

class UserAdditionReadWithProjectsEntity extends UUIDIdentifiable {

	public final String siteName;
	public final String projectName;
	public final String userId;
	public final int status;
	public final String code;
	public final String message;

	public UserAdditionReadWithProjectsEntity(String siteName, String projectName, String userId, int status, String code, String message) {
		this.siteName = siteName;
		this.projectName = projectName;
		this.userId = userId;
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
				Objects.equals(projectName, that.projectName) &&
				Objects.equals(userId, that.userId) &&
				Objects.equals(code, that.code) &&
				Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteName, projectName, userId, status, code, message);
	}

	@Override
	public String toString() {
		return "UserAdditionReadWithProjectsEntity{" +
				"siteName='" + siteName + '\'' +
				", projectName='" + projectName + '\'' +
				", userId='" + userId + '\'' +
				", status=" + status +
				", code=" + code +
				", message='" + message + '\'' +
				'}';
	}
}
