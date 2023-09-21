/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.performance.tests.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.imunity.furms.api.user.api.key.UserApiKeyService;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.rest.api.types.basic.RestAttribute;
import io.imunity.rest.api.types.basic.RestEntity;
import io.imunity.rest.api.types.basic.RestGroupMember;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.LongStream;

import static io.imunity.furms.domain.authz.roles.Role.PROJECT_ADMIN;
import static io.imunity.furms.performance.tests.SecurityUserUtils.createSecurityUser;
import static io.imunity.furms.unity.common.UnityConst.ENUMERATION;
import static io.imunity.furms.unity.common.UnityConst.STRING;
import static java.util.stream.Collectors.toSet;

class UserDataLoader {

	public static final int BIG_COMMUNITY_ADMINS_AMOUNT = 100;

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
	private final CommunityGroupsDAO communityDao;
	private final ProjectGroupsDAO projectGroupsDAO;

	UserDataLoader(UnityClient unityClient,
	               UserApiKeyService userApiKeyService,
	               CommunityGroupsDAO communityDao,
	               ProjectGroupsDAO projectGroupsDAO) {
		this.unityClient = unityClient;
		this.userApiKeyService = userApiKeyService;
		this.communityDao = communityDao;
		this.projectGroupsDAO = projectGroupsDAO;
	}

	Set<Data.User> loadUsers(final long usersCount) {
		return LongStream.range(0, usersCount)
			.parallel()
			.mapToObj(i -> createUser("Regular User"))
			.collect(toSet());
	}

	Set<Data.User> findAllUsers() {
		return unityClient.get(UriComponentsBuilder.newInstance()
				.path("/group-members/")
				.pathSegment("{groupPath}")
				.buildAndExpand(Map.of("groupPath", "/fenix/users"))
				.encode()
				.toUriString(), new ParameterizedTypeReference<Set<RestGroupMember>>() {})
				.stream()
				.filter(groupMember -> hasIdentity(groupMember, "identifier"))
				.map(groupMember -> new Data.User(
						getIdentity(groupMember, "persistent"),
						groupMember.entity.entityInformation.entityId,
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
			RestAttribute.builder()
				.withName("furmsFenixRole")
				.withValueSyntax(ENUMERATION)
				.withGroupPath("/fenix/users")
				.withValues(List.of("ADMIN"))
				.build());

		final PersistentId persistentId = new PersistentId(fenixAdmin.persistentId);
		final String apiKey = UUID.randomUUID().toString();
		userApiKeyService.save(persistentId, apiKey);

		return new Data.User(fenixAdmin.persistentId, fenixAdmin.entityId, fenixAdmin.fenixUserId, apiKey);
	}

	Data.User createCommunitiesAdmin(Data.User admin, Set<Data.Community> communities) {
		communities.forEach(community -> {
			communityDao.addAdmin(community.communityId, new PersistentId(admin.persistentId));
		});

		return new Data.User(admin.persistentId, admin.entityId, admin.fenixUserId, admin.apiKey);
	}

	public Data.User createProjectsAdmin(Data.User admin, Data.Community bigCommunity, Set<Data.Community> communities) {
		Set<Pair<CommunityId,ProjectId>> communityAndProjectIds = new HashSet<>();
		Random random = new Random();

		ProjectId[] objects = bigCommunity.projectIds.toArray(new ProjectId[0]);
		for(int i = 0; i < BIG_COMMUNITY_ADMINS_AMOUNT; i++){
			communityAndProjectIds.add(Pair.of(bigCommunity.communityId, objects[random.nextInt(bigCommunity.projectIds.size())]));
		}
		for(Data.Community community : communities){
			ProjectId[] tempIds = community.projectIds.toArray(new ProjectId[0]);
			communityAndProjectIds.add(Pair.of(community.communityId, tempIds[random.nextInt(community.projectIds.size())]));
		}

		communityAndProjectIds
				.forEach(id -> projectGroupsDAO.addProjectUser(
						id.getLeft(),
						id.getRight(),
						new PersistentId(admin.persistentId),
						PROJECT_ADMIN)
				);
		createSecurityUser(Map.of());

		return new Data.User(admin.persistentId, admin.entityId, admin.fenixUserId, admin.apiKey);
	}

	private Data.User createUser(final String role) {
		String userName = UUID.randomUUID().toString();

		String fenixId = userName.replace("-", "") + "@fenixId@acc.fenix.eduteams.org";
		EntityId id = createIdentityFenixId(userName);
		assignAttributes(id, userName, role);
		addUserToGroup(id, "/fenix/users");

		final String persistentId = unityClient.get(UriComponentsBuilder.newInstance()
				.path("/entity/")
				.pathSegment("{entityId}")
				.buildAndExpand(Map.of("entityId", String.valueOf(id.entityId)))
				.encode()
				.toUriString(), RestEntity.class)
				.identities.stream()
				.filter(identity -> identity.typeId.equals("persistent"))
				.findFirst()
				.get()
				.value;

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
		final Set<RestAttribute> attributes = Set.of(
			RestAttribute.builder()
				.withName("email")
				.withValueSyntax("verifiableEmail")
				.withGroupPath("/")
				.withValues(List.of("{\"value\":\"" + userName.replace("-", "") + "@domain.com\"," +
					"\"confirmationData\":{" +
					"\"confirmed\":true," +
					"\"confirmationDate\":1491257136061," +
					"\"sentRequestAmount\":0}," +
					"\"tags\":[]}"))
				.build(),
			RestAttribute.builder()
				.withName("firstname")
				.withValueSyntax(STRING)
				.withGroupPath("/")
				.withValues(List.of(userName))
				.build(),
			RestAttribute.builder()
				.withName("name")
				.withValueSyntax(STRING)
				.withGroupPath("/")
				.withValues(List.of("userName"))
				.build(),
			RestAttribute.builder()
				.withName("surname")
				.withValueSyntax(STRING)
				.withGroupPath("/")
				.withValues(List.of(userName))
				.build(),
			RestAttribute.builder()
				.withName("sys:AuthorizationRole")
				.withValueSyntax(ENUMERATION)
				.withGroupPath("/")
				.withValues(List.of(role))
				.build());
		unityClient.put(UriComponentsBuilder.newInstance()
				.path("/entity/")
				.pathSegment("{entityId}")
				.path("/attributes")
				.buildAndExpand(
						Map.of("entityId", String.valueOf(id.entityId)))
				.encode()
				.toUriString(), attributes, Map.of());
	}

	private EntityId createIdentityFenixId(String fenixId) {
		return unityClient.post(UriComponentsBuilder.newInstance()
						.path("/entity/identity/")
						.pathSegment("{type}", "{value}")
						.buildAndExpand(Map.of(
								"type", "identifier",
								"value", fenixId))
						.encode()
						.toUriString(), null,
				Map.of("credentialRequirement", "user%20password"),
				new ParameterizedTypeReference<>() {
				});
	}

	private boolean hasIdentity(RestGroupMember groupMember, String typeId) {
		return groupMember.entity.identities.stream()
			.anyMatch(identity -> identity.typeId.equals(typeId));
	}

	private String getIdentity(RestGroupMember groupMember, String typeId) {
		return groupMember.entity.identities.stream()
				.filter(identity -> identity.typeId.equals(typeId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Entity has to have " + typeId + " identity"))
				.value;
	}
}
