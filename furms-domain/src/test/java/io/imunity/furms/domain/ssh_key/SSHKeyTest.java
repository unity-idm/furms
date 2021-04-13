/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.ssh_key;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class SSHKeyTest {

	@Test
	public void shouldValidateCorrectKey() {
		assertDoesNotThrow(() -> SSHKey.validate(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/"
						+ "pnedckSpgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6x"
						+ "RKg3ONnxEXhJD/78OSp/ZY8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO"
						+ "2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tpbbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6"
						+ "w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5ALmb9xUkdFjZk9bL demo@demo.pl"));
	}

	@Test
	public void shouldThrowExceptionWhenIncorrectKey() {
		assertThrows(IllegalArgumentException.class,
				() -> SSHKey.validate("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvzxzzzzU"));
	}

	@Test
	public void shouldGetOptions() {
		Map<String, String> keyOptions = SSHKey.builder().value(
				"command=\"dump /home\",no-pty,no-port-forwarding, permitlisten=\"localhost:8080\" ,permitopen=\"192.0.2.1:80\" ,from=\"*.sales.example.net,!pc.sales.example.net\" ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/"
						+ "pnedckSpgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6x"
						+ "RKg3ONnxEXhJD/78OSp/ZY8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO"
						+ "2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tpbbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6"
						+ "w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5ALmb9xUkdFjZk9bL demo@demo.pl")
				.build().getKeyOptions();
		assertThat(keyOptions.get("from")).isEqualTo("*.sales.example.net,!pc.sales.example.net");
		assertThat(keyOptions.get("permitlisten")).isEqualTo("localhost:8080");
		assertThat(keyOptions.get("permitopen")).isEqualTo("192.0.2.1:80");
		assertThat(keyOptions.get("command")).isEqualTo("dump /home");
		assertThat(keyOptions.get("no-pty")).isEqualTo("true");
		assertThat(keyOptions.get("no-port-forwarding")).isEqualTo("true");
	}

}
