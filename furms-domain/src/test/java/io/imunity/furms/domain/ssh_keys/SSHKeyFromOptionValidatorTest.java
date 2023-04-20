/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SSHKeyFromOptionValidatorTest {

	@Test
	public void shouldValidateKeyOptionFromWithIPv4() {

		assertThrows(InvalidSSHKeyFromOptionException.class,
			() -> SSHKeyFromOptionValidator.validateFromOption("10.0.0.0"));
		assertThrows(InvalidSSHKeyFromOptionException.class,
			() -> SSHKeyFromOptionValidator.validateFromOption("\"10.0.0.0"));
		assertThrows(InvalidSSHKeyFromOptionException.class,
			() -> SSHKeyFromOptionValidator.validateFromOption("10.0.0.0\""));

		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKeyFromOptionValidator.validateFromOption("\"192.?.0.2\""));
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKeyFromOptionValidator.validateFromOption("\"192.168.0.2/15\""));
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKeyFromOptionValidator.validateFromOption("\"0.0.0.0\""));

		assertDoesNotThrow(() -> SSHKeyFromOptionValidator.validateFromOption("\"192.9.0.1/16\""));
		assertDoesNotThrow(() -> SSHKeyFromOptionValidator.validateFromOption("\"192.9.0.?\""));
	}

	@Test
	public void shouldValidateKeyOptionFromWithIPv6() {
		assertThrows(InvalidSSHKeyFromOptionException.class,
			() -> SSHKeyFromOptionValidator
				.validateFromOption("2001:0db8:0001:0000:0000:0ab9:C0A8:0102"));
		assertThrows(InvalidSSHKeyFromOptionException.class,
			() -> SSHKeyFromOptionValidator
				.validateFromOption("2001:0db8:0001:0000:0000:0ab9:C0A8:0102\""));
		assertThrows(InvalidSSHKeyFromOptionException.class,
			() -> SSHKeyFromOptionValidator
				.validateFromOption("\"2001:0db8:0001:0000:0000:0ab9:C0A8:0102"));

		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKeyFromOptionValidator.validateFromOption("::"));
		assertThrows(InvalidSSHKeyFromOptionException.class, () -> SSHKeyFromOptionValidator
				.validateFromOption("\"0000:0000:0000:0000:0000:0000:0000:0000\""));
		assertThrows(InvalidSSHKeyFromOptionException.class, () -> SSHKeyFromOptionValidator
				.validateFromOption("\"2001:0db8:0001:0000:0000:0ab9:C0A8:0102/40\""));

		assertThrows(InvalidSSHKeyFromOptionException.class, () -> SSHKeyFromOptionValidator
				.validateFromOption("\"2001:0db8:00?1:0000:0000:0ab9:C0A8:0102\""));
		assertThrows(InvalidSSHKeyFromOptionException.class, () -> SSHKeyFromOptionValidator
				.validateFromOption("\"2001:0db8:*:0000:0000:0ab9:C0A8:0102\""));

		assertDoesNotThrow(() -> SSHKeyFromOptionValidator
				.validateFromOption("\"2001:0db8:0001:0000:0000:0ab9:C0A8:0102\""));

	}

	@Test
	public void shouldNotValidateKeyOptionFromWithTLD() {
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKeyFromOptionValidator.validateFromOption("\"*.com.pl\""));
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKeyFromOptionValidator.validateFromOption("\"**.com.pl\""));
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKeyFromOptionValidator.validateFromOption("\"xx*.com.pl\""));
	}

	@Test
	public void shouldNotValidateKeyOptionFromWithWildcard() {
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKeyFromOptionValidator.validateFromOption("\"*\""));
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKeyFromOptionValidator.validateFromOption("\"**\""));

	}

	@Test
	public void shouldValidateKeyOptionFromWithoutTLD() {
		assertDoesNotThrow(() -> SSHKeyFromOptionValidator.validateFromOption("\"*tstdomain.com.pl\""));
		assertDoesNotThrow(() -> SSHKeyFromOptionValidator.validateFromOption("\"xx*tstdomain.com.pl\""));

	}

	@Test
	public void shouldNotValidateKeyOptionFromWithoutAnyValidHost() {
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKeyFromOptionValidator.validateFromOption("\"!192.1.0.2, !*test.com.pl\""));
	}
}
