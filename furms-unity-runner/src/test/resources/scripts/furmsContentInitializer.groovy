import com.google.common.collect.Lists
import groovy.transform.Field
import org.apache.commons.io.FilenameUtils
import org.springframework.core.io.Resource
import pl.edu.icm.unity.engine.api.config.UnityServerConfiguration
import pl.edu.icm.unity.exceptions.EngineException
import pl.edu.icm.unity.exceptions.IllegalIdentityValueException
import pl.edu.icm.unity.oauth.as.OAuthSystemAttributesProvider
import pl.edu.icm.unity.stdext.attr.*
import pl.edu.icm.unity.stdext.credential.pass.PasswordToken
import pl.edu.icm.unity.stdext.identity.UsernameIdentity
import pl.edu.icm.unity.stdext.utils.ContactEmailMetadataProvider
import pl.edu.icm.unity.stdext.utils.ContactMobileMetadataProvider
import pl.edu.icm.unity.stdext.utils.EntityNameMetadataProvider
import pl.edu.icm.unity.types.basic.*

@Field final String NAME_ATTR = "name"
@Field final String EMAIL_ATTR = "email"
@Field final String MOBILE_ATTR = "mobile"
@Field final String COMMON_ATTR_FILE = "common"

//run only if it is the first start of the server on clean DB.
if (!isColdStart)
{
	log.info("Database already initialized with content, skipping...")
	return
}
try
{
	initCommonAttrTypesFromResource()
	initDefaultAuthzPolicy()
	initCommonAttrTypes()
	initAuthAttrTypes()
	assignNameAttributeAndUserPasswordToAdminAccount()
	initBaseGroups()
	initRoleAttributeType()
	initOAuthClient()
	initTestUsers()
	initFurmsRestClient()

} catch (Exception e)
{
	log.warn("Error loading data", e)
}


void initCommonAttrTypesFromResource() throws EngineException
{
	List<Resource> resources = attributeTypeSupport.getAttibuteTypeResourcesFromClasspathDir()
	for (Resource r : resources)
		if (FilenameUtils.getBaseName(r.getFilename()).equals(COMMON_ATTR_FILE))
		{
			List<AttributeType> attrTypes = attributeTypeSupport
					.loadAttributeTypesFromResource(r)
			for (AttributeType type : attrTypes)
				attributeTypeManagement.addAttributeType(type)
			log.info("Common attributes added from resource file: " + r.getFilename())
		}
	log.info("Provisioned FURMS attribute types from resource")
}

void initAuthAttrTypes() throws EngineException
{
	def furmsSiteRole = new AttributeType("furmsSiteRole", EnumAttributeSyntax.ID, msgSrc)
	furmsSiteRole.setValueSyntaxConfiguration(new EnumAttributeSyntax("ADMIN", "SUPPORT")
			.getSerializedConfiguration())

	def furmsFenixRole = new AttributeType("furmsFenixRole", EnumAttributeSyntax.ID, msgSrc)
	furmsFenixRole.setValueSyntaxConfiguration(new EnumAttributeSyntax("ADMIN")
			.getSerializedConfiguration())

	def furmsCommunityRole = new AttributeType("furmsCommunityRole", EnumAttributeSyntax.ID, msgSrc)
	furmsCommunityRole.setValueSyntaxConfiguration(new EnumAttributeSyntax("ADMIN")
			.getSerializedConfiguration())

	def furmsProjectRole = new AttributeType("furmsProjectRole", EnumAttributeSyntax.ID, msgSrc)
	furmsProjectRole.setValueSyntaxConfiguration(new EnumAttributeSyntax("ADMIN", "MEMBER")
			.getSerializedConfiguration())

	[furmsSiteRole, furmsFenixRole, furmsCommunityRole, furmsProjectRole]
			.each{attributeTypeManagement.addAttributeType(it)}

	log.info("Provisioned default FURMS roles attributes types")
}

void initTestUsers()
{
	IdentityParam toAdd = new IdentityParam(UsernameIdentity.ID, "furms-site-demo-user")
	Identity base = entityManagement.addEntity(toAdd, EntityState.valid)
	EntityParam entity = new EntityParam(base.getEntityId())
	PasswordToken pToken = new PasswordToken("a")
	entityCredentialManagement.setEntityCredential(entity, "userPassword", pToken.toJson())

	log.info("Provisioned test FURMS users")
}

void initFurmsRestClient()
{
	IdentityParam toAdd = new IdentityParam(UsernameIdentity.ID, "furms-rest-client")
	Identity base = entityManagement.addEntity(toAdd, EntityState.valid)
	EntityParam entity = new EntityParam(base.getEntityId())

	Attribute role = EnumAttribute.of("sys:AuthorizationRole", "/", "Contents Manager")
	attributesManagement.createAttribute(entity, role)

	Attribute name = StringAttribute.of(NAME_ATTR, "/", "FURMS client user")
	attributesManagement.createAttribute(entity, name)

	PasswordToken clientPassword = new PasswordToken("AdminP@SSword1234!@&")
	entityCredentialManagement.setEntityCredential(entity, "clientPassword", clientPassword.toJson())

	log.info("Provisioned FURMS client users")
}

void initDefaultAuthzPolicy() throws EngineException
{
	//create attribute statement for the root group, which assigns regular user role
	//to all its members
	AttributeStatement everybodyStmt = AttributeStatement.getFixedEverybodyStatement(
			EnumAttribute.of("sys:AuthorizationRole", "/", "Regular User"))
	Group rootGroup = groupsManagement.getContents("/", GroupContents.METADATA).getGroup()
	AttributeStatement[] statements = [everybodyStmt]
	rootGroup.setAttributeStatements(statements)
	groupsManagement.updateGroup("/", rootGroup)
	log.info("Provisioned default FURMS authorization policy")
}


void initCommonAttrTypes() throws EngineException
{
	//here we create couple of useful attribute types, paying attention not to
	// create those which are already defined. This check shouldn't be necessary
	// when coldStart check is done, it is relevant only if this check is turned off.

	Map<String, AttributeType> existingATs = attributeTypeManagement.getAttributeTypesAsMap()

	//The name attribute will be marked as special attribute providing owner's displayed name.
	AttributeType name = new AttributeType(NAME_ATTR, StringAttributeSyntax.ID, msgSrc)
	name.setMinElements(1)
	StringAttributeSyntax nameSyntax = new StringAttributeSyntax()
	nameSyntax.setMaxLength(100)
	nameSyntax.setMinLength(2)
	name.setValueSyntaxConfiguration(nameSyntax.getSerializedConfiguration())
	name.getMetadata().put(EntityNameMetadataProvider.NAME, "")
	name.setGlobal(true)
	if (!existingATs.containsKey(NAME_ATTR))
		attributeTypeManagement.addAttributeType(name)

	//The email attribute will be marked as special attribute providing owner's contact e-mail.
	AttributeType verifiableEmail = new AttributeType(EMAIL_ATTR,
			VerifiableEmailAttributeSyntax.ID, msgSrc)
	verifiableEmail.setMinElements(1)
	verifiableEmail.setMaxElements(5)
	verifiableEmail.getMetadata().put(ContactEmailMetadataProvider.NAME, "")
	if (!existingATs.containsKey(EMAIL_ATTR))
		attributeTypeManagement.addAttributeType(verifiableEmail)

	//The mobile attribute will be marked as special attribute providing owner's contact mobile.
	AttributeType verifiableMobile = new AttributeType(MOBILE_ATTR,
			VerifiableMobileNumberAttributeSyntax.ID, msgSrc)
	verifiableMobile.setMinElements(1)
	verifiableMobile.setMaxElements(5)
	verifiableMobile.getMetadata().put(ContactMobileMetadataProvider.NAME, "")
	if (!existingATs.containsKey(MOBILE_ATTR))
		attributeTypeManagement.addAttributeType(verifiableMobile)
	log.info("Provisioned common(name, email, mobile) attribute types")
}

void assignNameAttributeAndUserPasswordToAdminAccount() throws EngineException
{
	//admin user has no "name" and password - let's assign one.
	String adminU = config.getValue(UnityServerConfiguration.INITIAL_ADMIN_USER)
	if (adminU == null) return
	Attribute nameA = StringAttribute.of(NAME_ATTR, "/", "Default Administrator")
	EntityParam entity = new EntityParam(new IdentityTaV(UsernameIdentity.ID, adminU))
	try
	{
		if (attributesManagement.getAttributes(entity, "/", NAME_ATTR).isEmpty())
		{
			attributesManagement.createAttribute(entity, nameA)
			PasswordToken userPassword = new PasswordToken("a")
			entityCredentialManagement.setEntityCredential(entity, "userPassword", userPassword.toJson())
		}
	} catch (IllegalIdentityValueException e)
	{
		//ok - no default admin, no default Name.
	}
	log.info("Assigned name attribute and user password to admin account()")
}

void initBaseGroups()
{
	groupsManagement.addGroup(new Group("/fenix"))
	groupsManagement.addGroup(new Group("/fenix/users"))
	groupsManagement.addGroup(new Group("/fenix/sites"))
	groupsManagement.addGroup(new Group("/fenix/communities"))
	log.info("Provisioned base Furms groups")
}

void initRoleAttributeType()
{
	AttributeType role = new AttributeType("role", "string")
	role.setMinElements(1)
	attributeTypeManagement.addAttributeType(role)
	log.info("Provisioned role attribute type")
}

void initOAuthClient()
{
	groupsManagement.addGroup(new Group("/oauth-clients"))
	IdentityParam oauthClient = new IdentityParam(UsernameIdentity.ID, "oauth-client")
	Identity oauthClientA = entityManagement.addEntity(oauthClient,
			EntityState.valid)
	PasswordToken pToken2 = new PasswordToken("oauth-pass1")

	EntityParam entityP = new EntityParam(oauthClientA.getEntityId())
	entityCredentialManagement.setEntityCredential(entityP, "userPassword", pToken2.toJson())
	log.warn("Furms OAuth client user was created with default password.  Please change it! U: oauth-client P: oauth-pass1")

	Attribute cnA = StringAttribute.of(NAME_ATTR, "/", "OAuth client")
	attributesManagement.createAttribute(entityP, cnA)

	groupsManagement.addMemberFromParent("/oauth-clients", entityP)
	Attribute flowsA = EnumAttribute.of(OAuthSystemAttributesProvider.ALLOWED_FLOWS,
			"/oauth-clients",
			Lists.newArrayList(
					OAuthSystemAttributesProvider.GrantFlow.authorizationCode.toString(), OAuthSystemAttributesProvider.GrantFlow.implicit.toString(),
					OAuthSystemAttributesProvider.GrantFlow.openidHybrid.toString()))
	attributesManagement.createAttribute(entityP, flowsA)
	Attribute returnUrlA = StringAttribute.of(OAuthSystemAttributesProvider.ALLOWED_RETURN_URI,
			"/oauth-clients",
			"https://localhost:2443/unitygw/oauth2ResponseConsumer",
			"https://localhost:3443/login/oauth2/code/unity"
	)
	attributesManagement.createAttribute(entityP, returnUrlA)
	log.info("Initialized all data required for oAuth2 client")
}