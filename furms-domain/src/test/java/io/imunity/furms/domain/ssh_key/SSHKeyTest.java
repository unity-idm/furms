/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.ssh_key;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.imunity.furms.domain.ssh_keys.InvalidSSHKeyFromOptionException;
import io.imunity.furms.domain.ssh_keys.SSHKey;

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
	
	@Test
	public void shouldValidateKeyOptionFrom() {		
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKey.validate(getKeyWithFrom("!192.1.0.2, !*test.com.pl")));
		
		
		//IPv4
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKey.validate(getKeyWithFrom("192.?.0.2")));
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKey.validate(getKeyWithFrom("192.168.0.2/15")));
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKey.validate(getKeyWithFrom("0.0.0.0")));	
		
		assertDoesNotThrow(() -> SSHKey.validate(getKeyWithFrom("192.9.0.1/16")));
		assertDoesNotThrow(() -> SSHKey.validate(getKeyWithFrom("192.9.0.?")));
		
		//IPv6
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKey.validate(getKeyWithFrom("::")));
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKey.validate(getKeyWithFrom("0000:0000:0000:0000:0000:0000:0000:0000")));
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKey.validate(getKeyWithFrom("2001:0db8:0001:0000:0000:0ab9:C0A8:0102/40")));
		
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKey.validate(getKeyWithFrom("2001:0db8:00?1:0000:0000:0ab9:C0A8:0102")));
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKey.validate(getKeyWithFrom("2001:0db8:*:0000:0000:0ab9:C0A8:0102")));
		
		assertDoesNotThrow(
				() -> SSHKey.validate(getKeyWithFrom("2001:0db8:0001:0000:0000:0ab9:C0A8:0102")));
		
		//TLD
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKey.validate(getKeyWithFrom("*.com.pl")));
		assertThrows(InvalidSSHKeyFromOptionException.class,
				() -> SSHKey.validate(getKeyWithFrom("xx*.com.pl")));
		assertDoesNotThrow(() -> SSHKey.validate(getKeyWithFrom("*tstdomain.com.pl")));
		assertDoesNotThrow(() -> SSHKey.validate(getKeyWithFrom("xx*tstdomain.com.pl")));
		
	
	}
	
	private String getKeyWithFrom(String from){
		
			return	"from=" + from + " ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/"
						+ "pnedckSpgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6x"
						+ "RKg3ONnxEXhJD/78OSp/ZY8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO"
						+ "2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tpbbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6"
						+ "w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5ALmb9xUkdFjZk9bL demo@demo.pl";
	}
	
}
