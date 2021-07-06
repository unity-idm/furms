/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class PolicyFile {
	private final byte[] file;
	private final PolicyFileType type;

	public PolicyFile(byte[] logoImage, String type) {
		this.file = logoImage;
		this.type = Optional.ofNullable(type)
			.map(String::toUpperCase)
			.map(PolicyFileType::valueOf)
			.orElse(null);
	}

	public byte[] getFile() {
		return file;
	}

	public boolean isEmpty(){
		return file.length == 0 && type == null;
	}

	public String getTypeExtension() {
		return Optional.ofNullable(type)
			.map(e -> e.extension)
			.map(String::toLowerCase)
			.orElse(null);
	}

	public static PolicyFile empty() {
		return new PolicyFile(new byte[0], null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyFile that = (PolicyFile) o;
		return Arrays.equals(file, that.file) &&
			type == that.type;
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(type);
		result = 31 * result + Arrays.hashCode(file);
		return result;
	}

	@Override
	public String toString() {
		return "PolicyFile{" + "type=" + type + '}';
	}
}
