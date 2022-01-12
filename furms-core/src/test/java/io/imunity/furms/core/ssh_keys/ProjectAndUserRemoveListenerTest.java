/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import com.google.common.collect.Sets;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectRemovedEvent;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserProjectMembershipRevokedEvent;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProjectAndUserRemoveListenerTest {

	@Mock
	private SSHKeyRepository repository;

	@Mock
	private UsersDAO usersDAO;

	@Mock
	private UserOperationRepository userOperationRepository;

	@Mock
	private SSHKeyFromSiteRemover sshKeyFromSiteRemover;

	@Mock
	private SSHKeyHistoryRepository sshKeyHistoryRepository;
	
	private ProjectAndUserRemoveListener listener;

	@BeforeEach
	void setUp() {
		listener = new ProjectAndUserRemoveListener(usersDAO, userOperationRepository, repository,
				sshKeyFromSiteRemover, sshKeyHistoryRepository);
	}

	@Test
	public void shouldProcessUserAfterRemoveFromProject() {

		UUID projectUUID = UUID.randomUUID();
		SSHKey key = SSHKey.builder().id("id").name("key").value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
						+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
						+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
						+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
						+ "mb9xUkdFjZk9bL demo@demo.pl")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet("s1", "s2")).build();

		when(usersDAO.getFenixUserId(new PersistentId("id"))).thenReturn(new FenixUserId("id"));
		when(userOperationRepository.findAllUserAdditions(new FenixUserId("id"))).thenReturn(
				Sets.newHashSet(UserAddition.builder().projectId(UUID.randomUUID().toString())
						.siteId(new SiteId("s1", "id")).build()));
		when(repository.findAllByOwnerId(new PersistentId("id"))).thenReturn(Sets.newHashSet(key));

		listener.onUserRoleRemove(new UserProjectMembershipRevokedEvent(new PersistentId("id"),
				new ResourceId(projectUUID, ResourceType.PROJECT), null));

		verify(sshKeyFromSiteRemover).removeKeyFromSites(key, Sets.newHashSet("s2"), new FenixUserId("id"));
		verify(sshKeyHistoryRepository).deleteLatest("s2", "id");
		verify(repository).update(SSHKey.builder().id("id").name("key").value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
						+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
						+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
						+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
						+ "mb9xUkdFjZk9bL demo@demo.pl")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet("s1")).build());
	}

	@Test
	public void shouldProcessUserAfterRemoveProject() {

		UUID projectUUID = UUID.randomUUID();
		SSHKey key = SSHKey.builder().id("id").name("key").value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
						+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
						+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
						+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
						+ "mb9xUkdFjZk9bL demo@demo.pl")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet("s1", "s2")).build();

		when(userOperationRepository.findAllUserAdditions(new FenixUserId("id"))).thenReturn(
				Sets.newHashSet(UserAddition.builder().projectId(UUID.randomUUID().toString())
						.siteId(new SiteId("s1", "id")).build()));
		when(repository.findAllByOwnerId(new PersistentId("id"))).thenReturn(Sets.newHashSet(key));

		listener.onProjectRemove(new ProjectRemovedEvent(
			Collections.singletonList(FURMSUser.builder().email("demo@test.com")
				.fenixUserId(new FenixUserId("id")).id(new PersistentId("id"))
				.build()), Project.builder().build()));

		verify(sshKeyFromSiteRemover).removeKeyFromSites(key, Sets.newHashSet("s2"), new FenixUserId("id"));
		verify(sshKeyHistoryRepository).deleteLatest("s2", "id");
		verify(repository).update(SSHKey.builder().id("id").name("key").value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
						+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
						+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
						+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
						+ "mb9xUkdFjZk9bL demo@demo.pl")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet("s1")).build());
	}

}
