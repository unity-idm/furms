/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.performance.tests.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.user.api.key.UserApiKeyService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.unity.client.UnityClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.Entity;
import pl.edu.icm.unity.types.basic.GroupMember;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.performance.tests.SecurityUserUtils.createSecurityUser;
import static io.imunity.furms.unity.common.UnityConst.ENUMERATION;
import static io.imunity.furms.unity.common.UnityConst.STRING;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

class UserDataLoader {

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class EntityId {
		@JsonProperty("entityId")
		public final long entityId;

		@JsonCreator
		public EntityId(@JsonProperty("entityId") long entityId) {
			this.entityId = entityId;
		}
	}

	private final UnityClient unityClient;
	private final UserApiKeyService userApiKeyService;
	private final CommunityService communityService;
	private final ProjectService projectService;

	UserDataLoader(UnityClient unityClient,
	               UserApiKeyService userApiKeyService,
	               CommunityService communityService,
	               ProjectService projectService) {
		this.unityClient = unityClient;
		this.userApiKeyService = userApiKeyService;
		this.communityService = communityService;
		this.projectService = projectService;
	}

	Set<Data.User> loadUsers(final long usersCount) {
		return LongStream.range(0, usersCount)
				.mapToObj(i -> createUser("Regular User"))
				.collect(toSet());
	}

	Set<Data.User> findAllUsers() {
		return unityClient.get(UriComponentsBuilder.newInstance()
				.path("/group-members/")
				.pathSegment("{groupPath}")
				.buildAndExpand(Map.of("groupPath", "/fenix/users"))
				.encode()
				.toUriString(), new ParameterizedTypeReference<Set<GroupMember>>() {})
				.stream()
				.filter(groupMember -> groupMember.getAttributes().stream()
						.anyMatch(attribute -> attribute.getName().equals("name")
								&& attribute.getValues().contains("createdInScript")))
				.map(groupMember -> new Data.User(
						getIdentity(groupMember, "persistent"),
						groupMember.getEntity().getId(),
						getIdentity(groupMember, "identifier")))
				.collect(toSet());
	}

	Data.User createFenixAdmin() {
		final Data.User fenixAdmin = createUser("System Manager");

		unityClient.put(UriComponentsBuilder.newInstance()
						.path("/entity/")
						.pathSegment("{entityId}")
						.path("/attribute")
						.buildAndExpand(
								Map.of("entityId", String.valueOf(fenixAdmin.entityId)))
						.encode()
						.toUriString(),
				new Attribute("furmsFenixRole", ENUMERATION, "/fenix/users", List.of("ADMIN")), Map.of());

		final PersistentId persistentId = new PersistentId(fenixAdmin.persistentId);
		final String apiKey = UUID.randomUUID().toString();
		userApiKeyService.save(persistentId, apiKey);

		return new Data.User(fenixAdmin.persistentId, fenixAdmin.entityId, fenixAdmin.fenixUserId, apiKey);
	}

	Data.User createCommunitiesAdmin(Set<Data.Community> communities) {
		final Data.User admin = createUser("Regular User");

		communities.forEach(community -> {
			communityService.addAdmin(community.communityId, new PersistentId(admin.persistentId));
		});

		final PersistentId persistentId = new PersistentId(admin.persistentId);
		final String apiKey = UUID.randomUUID().toString();
		userApiKeyService.save(persistentId, apiKey);

		return new Data.User(admin.persistentId, admin.entityId, admin.fenixUserId, apiKey);
	}

	public Data.User createProjectsAdmin(Map<Data.User, Set<Data.Project>> projectMemberships) {
		final Data.User admin = createUser("Regular User");

		createSecurityUser(projectMemberships.values().stream()
				.flatMap(Collection::stream)
				.map(project -> project.projectId)
				.distinct()
				.collect(toMap(
						projectId -> new ResourceId(projectId, PROJECT),
						projectId -> Set.of(Role.PROJECT_ADMIN))));

		projectMemberships.values().stream()
				.flatMap(Collection::stream)
				.collect(groupingBy(project -> project.communityId))
				.entrySet().stream()
				.map(entry -> entry.getValue().stream()
						.collect(groupingBy(Function.identity(), Collectors.counting())))
				.map(projects -> {
					final Long max = projects.values().stream()
							.mapToLong(x -> x)
							.max()
							.orElse(0);
					return projects.entrySet().stream()
							.filter(entry -> Objects.equals(entry.getValue(), max))
							.findAny()
							.map(Map.Entry::getKey);
				})
				.filter(Optional::isPresent)
				.map(Optional::get)
				.forEach(project -> projectService.addAdmin(
						project.communityId,
						project.projectId,
						new PersistentId(admin.persistentId)));

		final PersistentId persistentId = new PersistentId(admin.persistentId);
		final String apiKey = UUID.randomUUID().toString();
		userApiKeyService.save(persistentId, apiKey);

		createSecurityUser(Map.of());

		return new Data.User(admin.persistentId, admin.entityId, admin.fenixUserId, apiKey);
	}

	Data.User createUser(final String role) {
		final String userName = UUID.randomUUID().toString();

		final EntityId id = createIdentityUserName(userName);
		final String fenixId = assignFenixUserId(id, userName);
		assignAttributes(id, userName, role);
		addUserToGroup(id, "/fenix/users");

		final String persistentId = unityClient.get(UriComponentsBuilder.newInstance()
				.path("/entity/")
				.pathSegment("{entityId}")
				.buildAndExpand(Map.of("entityId", String.valueOf(id.entityId)))
				.encode()
				.toUriString(), Entity.class)
				.getIdentities().stream()
				.filter(identity -> identity.getTypeId().equals("persistent"))
				.findFirst()
				.get()
				.getValue();

		return new Data.User(persistentId, id.entityId, fenixId);
	}

	private void addUserToGroup(EntityId id, String group) {
		unityClient.post(UriComponentsBuilder.newInstance()
						.path("/group/")
						.pathSegment("{groupPath}")
						.path("/entity/")
						.pathSegment("{entityId}")
						.buildAndExpand(Map.of(
								"groupPath", group,
								"entityId", String.valueOf(id.entityId)))
						.encode()
						.toUriString(), null, Map.of(),
				new ParameterizedTypeReference<>() {
				});
	}

	private void assignAttributes(EntityId id, String userName, String role) {
		final Set<Attribute> attributes = Set.of(
				new Attribute("email", "verifiableEmail", "/",
						List.of("{\"value\":\"" + userName.replace("-", "") + "@domain.com\"," +
								"\"confirmationData\":{" +
									"\"confirmed\":true," +
									"\"confirmationDate\":1491257136061," +
									"\"sentRequestAmount\":0}," +
								"\"tags\":[]}")),
				new Attribute("firstname", STRING, "/", List.of(userName)),
				new Attribute("name", STRING, "/", List.of(userName)),
				new Attribute("surname", STRING, "/", List.of(userName)),
				new Attribute("sys:AuthorizationRole", ENUMERATION, "/", List.of(role)));
		unityClient.put(UriComponentsBuilder.newInstance()
				.path("/entity/")
				.pathSegment("{entityId}")
				.path("/attributes")
				.buildAndExpand(
						Map.of("entityId", String.valueOf(id.entityId)))
				.encode()
				.toUriString(), attributes, Map.of());
	}

	private String assignFenixUserId(EntityId id, String userName) {
		final String fenixId = userName.replace("-", "") + "@fenixId@acc.fenix.eduteams.org";
		unityClient.post(UriComponentsBuilder.newInstance()
						.path("/entity/")
						.pathSegment("{entityId}")
						.path("/identity/")
						.pathSegment("{type}", "{value}")
						.buildAndExpand(Map.of(
								"entityId", String.valueOf(id.entityId),
								"type", "identifier",
								"value", fenixId))
						.encode()
						.toUriString(), null, Map.of(),
				new ParameterizedTypeReference<>() {
				});
		return fenixId;
	}

	private EntityId createIdentityUserName(String userName) {
		return unityClient.post(UriComponentsBuilder.newInstance()
						.path("/entity/identity/")
						.pathSegment("{type}", "{value}")
						.buildAndExpand(Map.of(
								"type", "userName",
								"value", userName))
						.encode()
						.toUriString(), null,
				Map.of("credentialRequirement", "user%20password"),
				new ParameterizedTypeReference<>() {
				});
	}

	private String getIdentity(GroupMember groupMember, String typeId) {
		return groupMember.getEntity().getIdentities().stream()
				.filter(identity -> identity.getTypeId().equals(typeId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Entity has to have " + typeId + " identity"))
				.getValue();
	}
}
