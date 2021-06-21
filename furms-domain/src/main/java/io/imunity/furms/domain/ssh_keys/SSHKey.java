/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.digest.BuiltinDigests;

import com.google.common.base.CharMatcher;
import com.google.common.net.InternetDomainName;

import io.imunity.furms.domain.users.PersistentId;

public class SSHKey {
	public final String id;
	public final String name;
	public final String value;
	public final LocalDateTime createTime;
	public final LocalDateTime updateTime;
	public final PersistentId ownerId;
	public final Set<String> sites;

	private SSHKey(String id, String name, String value, PersistentId ownerId, LocalDateTime createTime,
			LocalDateTime updateTime, Set<String> sites) {
		this.id = id;
		this.name = name;
		this.value = value;
		this.ownerId = ownerId;
		this.createTime = createTime;
		this.updateTime = updateTime;
		this.sites = sites != null ? Set.copyOf(sites) : null;
	}

	@Override
	public String toString() {
		return "SSHKey" + "id=" + id + ", name='" + name + ", createTime='" + createTime + '\''
				+ ", updateTime='" + updateTime + '\'' + ", ownerId='" + ownerId + '\'' + ", sites="
				+ sites + '}';
	}

	public static SSHKey.SSHKeyBuilder builder() {
		return new SSHKey.SSHKeyBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SSHKey entity = (SSHKey) o;
		return Objects.equals(id, entity.id) && Objects.equals(name, entity.name)
				&& Objects.equals(value, entity.value) && Objects.equals(sites, entity.sites)
				&& Objects.equals(createTime, entity.createTime)
				&& Objects.equals(updateTime, entity.updateTime)
				&& Objects.equals(ownerId, entity.ownerId) && Objects.equals(sites, entity.sites);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, value, createTime, updateTime, ownerId, sites);
	}

	public Map<String, String> getKeyOptions() {
		return getKeyOptions(value);
	}

	public void validate() {
		validate(value);
	}

	public void validateFromOption() {
		validateFromOption(getKeyOptions().get("from"));
	}
	
	public static Map<String, String> getKeyOptions(String publicSSHKey) {

		validate(publicSSHKey);
		return AuthorizedKeyEntry.parseAuthorizedKeyEntry(publicSSHKey).getLoginOptions();
	}

	public String getFingerprint() {
		return getKeyFingerprint(value);
	}

	public static void validateFromOption(String fromOption) {
		if (fromOption == null || fromOption.isEmpty())
			return;		
		String[] options = fromOption.split(",");
		if (Stream.of(options).filter(o -> !o.startsWith("!")).findAny().isEmpty()) {
			throw new InvalidSSHKeyFromOptionException("Host in from option require");
		}

		for (String option : options) {

			if (option.equals("*")) {
				throw new InvalidSSHKeyFromOptionException("Only * wildcard is not valid host");
			}

			if (isIPv4(option)) {
				isValidIPv4(option);
			} else if (isIPv6(option)) {
				isValidIPv6(option);
			} else {
				isValidHostname(option);
			}
		}
	}

	private static boolean isIPv4(String input) {
		return CharMatcher.anyOf("0123456789.?*\\/").matchesAllOf(input);
	}

	private static boolean isIPv6(String input) {
		return CharMatcher.anyOf(":").matchesAnyOf(input);
	}

	private static void isValidIPv4(String input) {

		if (input.startsWith("0.0.0.0")) {
			throw new InvalidSSHKeyFromOptionException("0.0.0.0 is not valid host");
		}

		if (input.contains("/")) {
			String[] split = input.split("\\/");
			if (split.length != 2) {
				throw new InvalidSSHKeyFromOptionException("Invalid CIDR notation");
			}
			Integer mask;
			try {
				mask = Integer.valueOf(split[1]);
			} catch (Exception e) {
				throw new InvalidSSHKeyFromOptionException("Invalid CIDR notation");

			}

			if (mask < 16) {
				throw new InvalidSSHKeyFromOptionException("The subnet mask must be greater than 15");
			}
		}

		if (CharMatcher.anyOf("?*").matchesAnyOf(input)) {
			String[] split = input.split("\\.");

			if (split.length < 2 || CharMatcher.anyOf("?*").matchesAnyOf(split[0])
					|| CharMatcher.anyOf("?*").matchesAnyOf(split[1])) {
				throw new InvalidSSHKeyFromOptionException(
						"*? can be use after 16 bit in host address");
			}
		}

	}

	private static void isValidIPv6(String input) {

		if (input.startsWith("::") || input.startsWith("0000:0000:0000:0000:0000:0000:0000:0000")) {
			throw new InvalidSSHKeyFromOptionException(":: is not valid host");
		}
		if (input.contains("/")) {
			String[] split = input.split("\\/");
			if (split.length != 2) {
				throw new InvalidSSHKeyFromOptionException("Invalid CIDR notation");
			}
			if (Integer.valueOf(split[1]) < 60) {
				throw new InvalidSSHKeyFromOptionException("The subnet mask must be greater than 59");
			}
		} else if (CharMatcher.anyOf("?*").matchesAnyOf(input)) {
			String[] split = input.split("\\:");
			if (split.length < 4 || CharMatcher.anyOf("?*").matchesAnyOf(split[0])
					|| CharMatcher.anyOf("?*").matchesAnyOf(split[1])
					|| CharMatcher.anyOf("?*").matchesAnyOf(split[2])
					|| CharMatcher.anyOf("?*").matchesAnyOf(split[3])) {
				throw new InvalidSSHKeyFromOptionException(
						"*? can be use after 60 bit in host address");
			}
		}
	}

	private static void isValidHostname(String input) {
		if (CharMatcher.anyOf("*").matchesAnyOf(input)) {
			String[] split = input.split("\\*");
			String last = split[split.length - 1];
			if (last.isEmpty()) {
				throw new InvalidSSHKeyFromOptionException(
						"* wildcard can be use only with domain name");
			} else {
				InternetDomainName idm;
				try {
					idm = InternetDomainName.from(CharMatcher.is('.').trimLeadingFrom(last));
				} catch (Exception e) {
					throw new InvalidSSHKeyFromOptionException(
							"* wildcard can be use only with domain name");
				}

				if (idm.isRegistrySuffix()) {

					throw new InvalidSSHKeyFromOptionException(
							"* wildcard can not be use with top level domain name");
				}
			}
		}
	}

	public static String getKeyFingerprint(String publicSSHKey) {

		try {
			return KeyUtils.getFingerPrint(BuiltinDigests.md5, AuthorizedKeyEntry
					.parseAuthorizedKeyEntry(publicSSHKey).resolvePublicKey(null, null));
		} catch (Exception e) {
			throw new InvalidSSHKeyValueException("Invalid SSH key value", e);
		}
	}

	public static void validate(String publicSSHKey) {
		if (publicSSHKey == null || publicSSHKey.isEmpty())
			throw new InvalidSSHKeyValueException("Invalid SSH Key value: SSH Key value is empty.");
		AuthorizedKeyEntry.parseAuthorizedKeyEntry(publicSSHKey);
		getKeyFingerprint(publicSSHKey);
	}

	public static class SSHKeyBuilder {

		private String id;
		private String name;
		private String value;
		private LocalDateTime createTime;
		private LocalDateTime updateTime;
		private PersistentId ownerId;
		private Set<String> sites;

		public SSHKey.SSHKeyBuilder id(String id) {
			this.id = id;
			return this;
		}

		public SSHKey.SSHKeyBuilder name(String name) {
			this.name = name;
			return this;
		}

		public SSHKey.SSHKeyBuilder value(String value) {
			this.value = value;
			return this;
		}

		public SSHKey.SSHKeyBuilder createTime(LocalDateTime createTime) {
			this.createTime = createTime;
			return this;
		}

		public SSHKey.SSHKeyBuilder updateTime(LocalDateTime updateTime) {
			this.updateTime = updateTime;
			return this;
		}

		public SSHKey.SSHKeyBuilder ownerId(PersistentId ownerId) {
			this.ownerId = ownerId;
			return this;
		}

		public SSHKey.SSHKeyBuilder sites(Set<String> sites) {
			this.sites = sites;
			return this;
		}

		public SSHKey build() {
			return new SSHKey(id, name, value, ownerId, createTime, updateTime, sites);
		}
	}
}
