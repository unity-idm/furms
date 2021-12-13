/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import com.google.common.base.CharMatcher;
import com.google.common.net.InternetDomainName;
import io.imunity.furms.domain.ssh_keys.InvalidSSHKeyFromOptionException.ErrorType;

import java.util.stream.Stream;

public class SSHKeyFromOptionValidator {

	private static final int VALID_IPv4_SUBNET_MASK = 16;
	private static final int VALID_IPv6_SUBNET_MASK = 60;

	public static void validateFromOption(String fromOption) {
		if (fromOption == null || fromOption.isEmpty())
			return;
		String[] options = fromOption.split(",");
		if (Stream.of(options).filter(o -> !o.trim().startsWith("!")).findAny().isEmpty()) {
			throw new InvalidSSHKeyFromOptionException("Host in from option required", fromOption,
					ErrorType.INVALID_HOST);
		}

		for (String option : options) {

			if (CharMatcher.anyOf("?*").matchesAllOf(option)) {
				throw new InvalidSSHKeyFromOptionException(
						"Using \"*\" or \"?\" as an allowed host address is prohibited.",
						fromOption, ErrorType.WILDCARD);
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
			throw new InvalidSSHKeyFromOptionException(
					"Using \"0.0.0.0\" as an allowed host address is prohibited.", input,
					ErrorType.NON_ROUTEABLE_HOST);
		}

		if (input.contains("/")) {
			String[] split = input.split("\\/");
			if (split.length != 2) {
				throw new InvalidSSHKeyFromOptionException("Invalid CIDR notation", input,
						ErrorType.CIDR_MASK);
			}
			int mask;
			try {
				mask = Integer.parseInt(split[1]);
			} catch (Exception e) {
				throw new InvalidSSHKeyFromOptionException("Invalid CIDR notation", input,
						ErrorType.CIDR_MASK);

			}

			if (mask < VALID_IPv4_SUBNET_MASK) {
				throw new InvalidSSHKeyFromOptionException(
						"The subnet mask must be greater than or equal to "
								+ VALID_IPv4_SUBNET_MASK,
						input, ErrorType.CIDR_MASK);
			}
		}

		if (CharMatcher.anyOf("?*").matchesAnyOf(input)) {
			String[] split = input.split("\\.");

			if (split.length < 2 || CharMatcher.anyOf("?*").matchesAnyOf(split[0])
					|| CharMatcher.anyOf("?*").matchesAnyOf(split[1])) {
				throw new InvalidSSHKeyFromOptionException(
						"*? can be used after 16 bit in host address", input,
						ErrorType.WILDCARD_IN_ADDRESS);
			}
		}

	}

	private static void isValidIPv6(String input) {

		if (input.startsWith("::") || input.startsWith("0000:0000:0000:0000:0000:0000:0000:0000")) {
			throw new InvalidSSHKeyFromOptionException(
					"Using \"::\" as an allowed host address is prohibited.", input,
					ErrorType.NON_ROUTEABLE_HOST);
		}
		if (input.contains("/")) {
			String[] split = input.split("\\/");
			if (split.length != 2) {
				throw new InvalidSSHKeyFromOptionException("Invalid CIDR notation", input,
						ErrorType.CIDR_MASK);
			}
			if (Integer.parseInt(split[1]) < VALID_IPv6_SUBNET_MASK) {
				throw new InvalidSSHKeyFromOptionException(
						"The subnet mask must be greater than or equal to "
								+ VALID_IPv6_SUBNET_MASK,
						input, ErrorType.CIDR_MASK);
			}
		} else if (CharMatcher.anyOf("?*").matchesAnyOf(input)) {
			String[] split = input.split("\\:");
			if (split.length < 4 || CharMatcher.anyOf("?*").matchesAnyOf(split[0])
					|| CharMatcher.anyOf("?*").matchesAnyOf(split[1])
					|| CharMatcher.anyOf("?*").matchesAnyOf(split[2])
					|| CharMatcher.anyOf("?*").matchesAnyOf(split[3])) {
				throw new InvalidSSHKeyFromOptionException(
						"*? can be used after 60 bit in host address", input,
						ErrorType.WILDCARD_IN_ADDRESS);
			}
		}
	}

	private static void isValidHostname(String input) {
		if (CharMatcher.anyOf("*").matchesAnyOf(input)) {
			String[] split = input.split("\\*");
			String last = split[split.length - 1];
			if (last.isEmpty()) {
				throw new InvalidSSHKeyFromOptionException(
						"* wildcard can be used only with domain name", input, ErrorType.WILDCARD_WITH_TLD);
			} else {
				InternetDomainName idm;
				try {
					idm = InternetDomainName.from(CharMatcher.is('.').trimLeadingFrom(last));
				} catch (Exception e) {
					throw new InvalidSSHKeyFromOptionException(
							"* wildcard can be used only with domain name", input, ErrorType.WILDCARD_WITH_TLD);
				}

				if (idm.isRegistrySuffix()) {

					throw new InvalidSSHKeyFromOptionException(
							"* wildcard can not be used with top level domain name", input, ErrorType.WILDCARD_WITH_TLD);
				}
			}
		}
	}
}
