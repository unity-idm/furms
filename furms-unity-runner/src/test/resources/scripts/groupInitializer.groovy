/*
 * Script with demonstration data of Unity server.
 * Should be used in test instalations only to have example contents just 
 * after initial startup.
 *
 * Depends on defaultContentInitializer.groovy
 */

import pl.edu.icm.unity.types.basic.AttributeType
import pl.edu.icm.unity.types.basic.Group
import pl.edu.icm.unity.types.basic.GroupContents
import groovy.transform.Field


@Field final String NAME_ATTR = "name"
@Field final String EMAIL_ATTR = "email";


if (!isColdStart)
{
	log.info("Database already initialized with content, skipping...");
	return;
}

log.info("Creating Furms content...");

try
{
	GroupContents rootContents = groupsManagement.getContents("/", GroupContents.GROUPS);
	if (rootContents.getSubGroups().contains("/project"))
	{
		log.error("Seems that init contents is installed, skipping");
		return;
	}
	
	Map<String, AttributeType> existingATs = attributeTypeManagement.getAttributeTypesAsMap();
	if (!existingATs.containsKey(NAME_ATTR) || !existingATs.containsKey(EMAIL_ATTR))
	{
		log.error("Furms contents can be only installed if standard types were installed " +
			"prior to it. Attribute types cn, o and email are required.");
		return;
	}
	
	createBaseGroups()
	createBaseAttributeTypes()
	
} catch (Exception e)
{
	log.warn("Error loading demo contents. This can happen and by far is not critical. " +
			"It means that demonstration contents was not loaded to your database, " +
			"usaully due to conflict with its existing data", e);
}


void createBaseGroups()
{
	groupsManagement.addGroup(new Group("/project"))
	groupsManagement.addGroup(new Group("/project/user"))
	groupsManagement.addGroup(new Group("/project/admin"))
	groupsManagement.addGroup(new Group("/community"))
	groupsManagement.addGroup(new Group("/community/admin"))
	groupsManagement.addGroup(new Group("/fenix"))
	groupsManagement.addGroup(new Group("/fenix/admin"))
	groupsManagement.addGroup(new Group("/site"))
	groupsManagement.addGroup(new Group("/site/admin"))
}

void createBaseAttributeTypes()
{
	AttributeType role = new AttributeType("role", "string")
	role.setMinElements(1)
	attributeTypeManagement.addAttributeType(role)
}