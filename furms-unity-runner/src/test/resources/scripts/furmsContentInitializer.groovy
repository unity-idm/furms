import com.google.common.collect.Lists
import groovy.transform.Field
import org.apache.commons.io.FilenameUtils
import org.springframework.core.io.Resource
import pl.edu.icm.unity.engine.api.config.UnityServerConfiguration
import pl.edu.icm.unity.engine.server.EngineInitialization
import pl.edu.icm.unity.exceptions.EngineException
import pl.edu.icm.unity.exceptions.IllegalIdentityValueException
import pl.edu.icm.unity.oauth.as.OAuthSystemAttributesProvider
import pl.edu.icm.unity.stdext.attr.EnumAttribute
import pl.edu.icm.unity.stdext.attr.StringAttribute
import pl.edu.icm.unity.stdext.attr.StringAttributeSyntax
import pl.edu.icm.unity.stdext.attr.VerifiableEmailAttributeSyntax
import pl.edu.icm.unity.stdext.attr.VerifiableMobileNumberAttributeSyntax
import pl.edu.icm.unity.stdext.credential.pass.PasswordToken
import pl.edu.icm.unity.stdext.identity.UsernameIdentity
import pl.edu.icm.unity.stdext.utils.ContactEmailMetadataProvider
import pl.edu.icm.unity.stdext.utils.ContactMobileMetadataProvider
import pl.edu.icm.unity.stdext.utils.EntityNameMetadataProvider
import pl.edu.icm.unity.types.basic.Attribute
import pl.edu.icm.unity.types.basic.AttributeStatement
import pl.edu.icm.unity.types.basic.AttributeType
import pl.edu.icm.unity.types.basic.EntityParam
import pl.edu.icm.unity.types.basic.EntityState
import pl.edu.icm.unity.types.basic.Group
import pl.edu.icm.unity.types.basic.GroupContents
import pl.edu.icm.unity.types.basic.Identity
import pl.edu.icm.unity.types.basic.IdentityParam
import pl.edu.icm.unity.types.basic.IdentityTaV

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
    initCommonAttrTypes()
    initDefaultAuthzPolicy()
    initCommonAttributeTypes()
    assignNameAttributeToAdminAccound()
    initBaseGroups()
    initBaseAttributeTypes()
    initOAuthClient()

} catch (Exception e)
{
    log.warn("Error loading data", e)
}


void initCommonAttrTypes() throws EngineException
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
    log.info("Finished initCommonAttrTypes()")
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
    log.info("Finished initDefaultAuthzPolicy()")
}


void initCommonAttributeTypes() throws EngineException
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
    log.info("Finished initCommonAttributeTypes()")
}

void assignNameAttributeToAdminAccound() throws EngineException {
    //admin user has no "name" - let's assign one.
    String adminU = config.getValue(UnityServerConfiguration.INITIAL_ADMIN_USER)
    if (adminU == null)
        return
    Attribute nameA = StringAttribute.of(NAME_ATTR, "/", "Default Administrator")
    EntityParam entity = new EntityParam(new IdentityTaV(UsernameIdentity.ID, adminU))
    try {
        if (attributesManagement.getAttributes(entity, "/", NAME_ATTR).isEmpty())
            attributesManagement.createAttribute(entity, nameA)
    } catch (IllegalIdentityValueException e) {
        //ok - no default admin, no default Name.
    }
    log.info("Finished assignNameAttributeToAdminAccound()")
}

void initBaseGroups()
{
    groupsManagement.addGroup(new Group("/fenix"))
    groupsManagement.addGroup(new Group("/fenix/users"))
    groupsManagement.addGroup(new Group("/fenix/sites"))
    groupsManagement.addGroup(new Group("/fenix/communities"))
    log.info("Finished initBaseGroups()")
}

void initBaseAttributeTypes()
{
    AttributeType role = new AttributeType("role", "string")
    role.setMinElements(1)
    attributeTypeManagement.addAttributeType(role)
    log.info("Finished initBaseAttributeTypes()")
}

void initOAuthClient()
{
    groupsManagement.addGroup(new Group("/oauth-clients"))
    IdentityParam oauthClient = new IdentityParam(UsernameIdentity.ID, "oauth-client")
    Identity oauthClientA = entityManagement.addEntity(oauthClient,
            EntityState.valid)
    PasswordToken pToken2 = new PasswordToken("oauth-pass1")

    EntityParam entityP = new EntityParam(oauthClientA.getEntityId())
    entityCredentialManagement.setEntityCredential(entityP, EngineInitialization.DEFAULT_CREDENTIAL, pToken2.toJson())
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
            "https://localhost:2443/unitygw/oauth2ResponseConsumer")
    attributesManagement.createAttribute(entityP, returnUrlA)
    log.info("Finished initOAuthClient()")
}