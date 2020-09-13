/*
/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorcer.core;

import java.util.concurrent.TimeUnit;

/**
 * SORCER interface provides predefined constants, commands and parameter names
 * for the SORCER metacomputing environment. Use them to allow for
 * interoperability between different classes, in particular to communicate
 * between requestors (clients) and providers (servers).
 */
@SuppressWarnings("unused")
public interface SorcerConstants {
	// SORCER provider property names
	// P_ATTRIBUTE is a provider property defined in a properties file
	// J_ATTRIBURE is variable name in Jini configuration file
	/* service provider generic properties */
	String P_UNDEFINED = "undefined";

    String E_SORCER_MODELING = "SORCER_MODELING";

    String S_SORCER_MODELING = "sorcer.modeling";

	String P_PROVIDER_NAME = "provider.name";

	String  EXERT_MONITOR_NAME = "provider.exert.monitor.name";
	
	String  DATABASE_STORER_NAME = "database.storer.name";
	
	String  DATASPACE_STORER_NAME = "dataspace.storer.name";

	String  SPACER_NAME = "provider.spacer.name";

	String P_PROVIDER_CONFIG = "provider.config.filename";

	String J_SERVICE_PROVIDER_NAME = "serviceName";

	String J_PROVIDER_NAME = "name";

	String J_PROVIDER_NAME_SUFFIXED = "nameSuffixed";

	String P_PROVIDR_HOST = "provider.host";

	String P_PROVIDR_ADDRESS = "provider.address";

	String J_PROVIDR_ADDRESS = "providerAddress";

	String J_PROVIDR_HOST = "providerHost";

	String P_PROXY_CLASS = "provider.proxy.class";

	String J_PROXY_CLASS = "providerProxy";

	String SERVER_EXPORTER = "serverExporter";

	String P_EXPORTER_INTERFACE = "provider.exporter.interface";
		
	String P_EXPORTER_PORT = "provider.exporter.port";

	String P_ICON_NAME = "provider.icon.name";

	String J_ICON_NAME = "iconName";

	String P_DESCRIPTION = "provider.description";

	String J_DESCRIPTION = "description";

	String P_LOCATION = "provider.location";

	String J_LOCATION = "location";

	String P_INTERFACES = "provider.published.interfaces";

	String J_INTERFACES = "publishedInterfaces";
	
	String J_SINGLE_TRHREADED_MODEL = "singleThreadModel";

	String J_PROVIDER_IS_PROVISINABLE = "isProvisionable";
	
	String P_CONTEXT_LOGGER = "sorcer.service.context";

	String PRIVATE_CONTEXT_LOGGER = "private.context";

	String PRIVATE_PROVIDER_LOGGER = "private.provider";

	String P_PORTAL_HOST = "provider.portal.host";

	String J_PORTAL_HOST = "portalHost";

	String P_PORTAL_PORT = "provider.portal.port";

	String J_PORTAL_PORT = "portalPort";

	String P_POOL_SIZE = "provider.pool.size";

	String J_POOL_SIZE = "poolSize";

	String P_WORKERS_MAX = "provider.workers.max";

	String J_WORKERS_MAX = "providerWorkersMax";

	String P_DELAY_TIME = "provider.exec.delay";

	String J_DELAY_TIME = "providerExecDelay";

	String P_QOSPOOL_SIZE = "provider.qospool.size";

	String P_QOSWORKERS_MAX = "provider.qosworkers.max";

	// code server, constants with sorcer.* prefix recommended as system
	// properties
	String SORCER_WEBSTER_INTERNAL = "sorcer.webster.internal";

	String SORCER_CODE_SERVER_INTERNAL = "ssb.codeserver.internal";

	String WEBSTER_ROOTS = "sorcer.webster.roots";

	String J_WEBSTER_HANDLER = "websterHandler";

	String P_WEBSTER_INTERFACE = "provider.webster.interface";

	String R_WEBSTER_INTERFACE = "requester.webster.interface";

	String R_WEBSTER_PORT = "requestor.webster.port";
	
	String S_WEBSTER_INTERFACE = "system.webster.interface";

	String J_WEBSTER_INTERFACE = "websterInterface";

	String P_WEBSTER_PORT = "provider.webster.port";

	String S_WEBSTER_PORT = "webster.port";

	String J_WEBSTER_PORT = "websterPort";

	String P_WEBSTER_START_PORT = "provider.webster.start.port";

	String S_WEBSTER_START_PORT = "webster.start.port";

	String J_WEBSTER_START_PORT = "websterStartPort";

	String P_WEBSTER_END_PORT = "provider.webster.end.port";
	
	String S_WEBSTER_END_PORT = "webster.end.port";

	String J_WEBSTER_END_PORT = "websterEndPort";

	// used by HTTP data server
	String DATA_SERVER_INTERFACE = "data.server.interface";

	String DATA_SERVER_PORT = "data.server.port";

	String DATA_SERVER_USE_HTTPS = "data.server.useHttps";

	String P_DATA_SERVER_INTERFACE = "provider.data.server.interface";

	String P_DATA_SERVER_PORT = "provider.data.server.port";

	String R_DATA_SERVER_INTERFACE = "requestor.data.server.interface";

	String R_DATA_SERVER_PORT = "requestor.data.server.port";

	String DOC_ROOT_DIR = "doc.root.dir";

	String P_DATA_ROOT_DIR = "provider.root.dir";

	String P_DATA_DIR = "provider.data.dir";

	String R_DATA_ROOT_DIR = "requestor.root.dir";

	String R_DATA_DIR = "requestor.data.dir";

	String SCRATCH_DIR = "scratch.dir";

	String P_SCRATCH_DIR = "provider.scratch.dir";

	String R_SCRATCH_DIR = "requestor.scratch.dir";

	String J_SCRATCH_DIR = "scratchDir";

	String P_DATA_LIMIT = "provider.data.limit";

	String J_DATA_LIMIT = "limit";

	/* provider environment */
	
	String P_SUFFIX = "sorcer.provider.name.suffix";
	
	String P_GROUPS = "provider.groups";

	String J_GROUPS = "providerGroups";

	String P_CATALOGER_NAME = "provider.catalog.name";
	
	String J_CATALOG_NAME = "catalogName";
	
	String P_SPACE_NAME = "provider.space.name";

	String J_SPACE_NAME = "spaceName";

	String P_SPACE_GROUP = "provider.space.group";

	String J_SPACE_GROUP = "spaceGroup";

	// locators for unicast discovery
	String P_LOCATORS = "provider.lookup.locators";

	String J_LOCATORS = "locators";

	String MULTICAST_ENABLED = "sorcer.multicast.enabled";

	// persist and reuse service ID
	String P_SERVICE_ID_PERSISTENT = "provider.id.persistent";

	String J_SERVICE_ID_PERSISTENT = "providerIdPersistent";

	String P_TEMPLATE_MATCH = "provider.template.match";

	String J_TEMPLATE_MATCH = "templateMatch";

	// SORCER global properties defined in sorcer.util.Sorcer.java
	String S_HOME = "sorcer.home";

	String S_ENV_FIENAME = "sorcer.env";

	String S_SERVICE_ID_FILENAME = "service.id.filename";

	String S_RMI_HOST = "sorcer.rmi.host";

	String S_RMI_PORT = "sorcer.rmi.port";

	String S_PERSISTER_IS_DB_TYPE = "sorcer.is.db.persistent"; // boolean

	String S_IS_DB_ORACLE = "sorcer.is.db.oracle";

	String S_NAME_SUFFIX = "sorcer.provider.name.suffix";

	String S_IS_NAME_SUFFIXED = "sorcer.provider.name.suffixed";
	
	String S_PERSISTER_NAME = "sorcer.persister.service";

	String S_JOBBER_NAME = "sorcer.jobber.name";

	String S_MODELER_NAME = "sorcer.modeler.name";

	String S_CONCATENATOR_NAME = "sorcer.concatenator.name";

	String S_EXERTER_NAME = "sorcer.exerter.name";

	String S_SPACER_NAME = "sorcer.spacer.name";

	String S_CATALOGER_NAME = "sorcer.cataloger.name";

	String S_COMMANDER_NAME = "sorcer.commander.name";

	String S_SERVICE_ACCESSOR_PROVIDER_NAME = "provider.lookup.accessor";

	String SORCER_HOME = "sorcer.home";

	// discovery and lookup

	String LOOKUP_WAIT = "lookup.wait";

	String LOOKUP_CACHE_ENABLED = "lookup.cache.enabled";

	String LOOKUP_MIN_MATCHES = "lookup.minMatches";

	String LOOKUP_MAX_MATCHES = "lookup.maxMatches";

	/**
	 * SORCER Notifier Message Indexing Constants used by sorcer.notifier.
	 * NotificationRetrievalListener* and the launcher
	 */
	int MSG_ID = 0;

	int MSG_TYPE = 1;

	int JOB_ID = 2;

	int TASK_ID = 3;

	int MSG_CONTENT = 4;

	int MSG_SOURCE = 5;

	int JOB_NAME = 6;

	int TASK_NAME = 7;

	int CREATION_TIME = 8;

	int IS_NEW = 9;

	int GETALL_DOMAIN_SUB = 601;

	int GET_CONTEXT = 600;

	int GET_CONTEXT_NAMES = 602;

	int PERSIST_CONTEXT = 603;

	int UPDATE_CONTEXT = 605;

	int ADD_DOMAIN = 606;

	int ADD_SUBDOMAIN = 607;

	int UPDATE_DATANODE = 608;

	int REMOVE_DATANODE = 609;

	int REMOVE_CONTEXT = 610;

	int RENAME_CONTEXT = 611;

	int ADD_DATANODE = 613;

	int PERSIST_JOB = 614;

	int DELETE_TASK = 615;

	int ADD_TASK = 616;

	int GET_JOB = 618;

	int GET_JOBDOMAIN = 619;

	int REMOVE_JOB = 620;

	int SAVE_EXERTION_AS_RUNTIME = 623;

	int ADD_TASK_TO_JOB_SAVEAS_RUNTIME = 625;

	int UPDATE_JOB = 629;

	int UPDATE_TASK = 630;

	int GET_JOBNAMES = 632;

	int GET_TASK_NAMES = 634;

	int REMOVE_TASK = 636;

	int SAVEJOB_AS = 637;

	int ADD_TASK_TO_JOB_SAVEAS = 638;

	int GET_TASK = 639;

	int UPDATE_EXERTION = 640;

	int SAVE_TASK_AS = 641;

	int GET_FT = 650;

	/**
	 * SORCER Notifier Notification Types
	 */
	int NOTIFY_FAILURE = 700;

	int NOTIFY_EXCEPTION = 701;

	int NOTIFY_INFORMATION = 702;

	int NOTIFY_WARNING = 703;
 
	/**
	 * SORCER task priorities
	 */
	int MIN_PRIORITY = 0;

	int NORMAL_PRIORITY = 5;

	int MAX_PRIORITY = 100;

	/*
	 * SORCER Method Type
	 */
	int Command = 0;

	int Script = 1;

	int Order = 2;

	/**
	 * SORCER common names
	 */
	// String BGCOLOR="C0C0C0";
	String BGCOLOR = "FFFFFF";

	String SELECT = "Select";

	// SorcerContext Ontology
	String CONTEXT_ATTRIBUTE_VALUES = "values";

	/** context path separator */
	String CPS = "/";

	/**
	 * context association path separator, for attribute descriptors:
	 * "result|operation|arg1|arg2", where result is a composite attribute and
	 * their associations "result|add|3|5" telling that the result is associated
	 * with the path that references 'add" operation of arguments 3 and 5 in a
	 * given service context.
	 */
	String APS = "|";

	String PRIVATE = "_";

	// ***Don't change this!***
	String CONTEXT_ATTRIBUTES = PRIVATE + "attributes" + PRIVATE;

	String IND = "index";

	String OUT_VALUE = "out" + CPS + "value";

	String SCRATCH_DIR_KEY = OUT_VALUE + CPS + "scratchDir";
	String SCRATCH_URL_KEY = OUT_VALUE + CPS + "scratchUrl";

	String OUT_COMMENT = "out" + CPS + "comment";

	String EXCEPTIONS = "exceptions";

	String EXCEPTION_OBJ = EXCEPTIONS + CPS + "exception" + IND
			+ CPS;

	String EXCEPTION_ST = EXCEPTIONS + CPS + "stack trace" + IND
			+ CPS;

	String OUT_PATH = "out/path";

	String OUT_FILE = "out/filename";

	String OUT_SCRIPT = "out/path/script";

	String VALUE = "value";

	String IN_VALUE = "in" + CPS + "value";

	String IN_PATH = "in" + CPS + "path";

	String IN_FILE = "in" + CPS + "filename";

	String SCRIPT = "script";

	String IN_SCRIPT = "in" + CPS + "path" + CPS + "script";

	String JOB_TASK = "job" + CPS + "task";

	String JOB_STATE = "job" + CPS + "state";

	String TASK_PROVIDER = "task" + CPS + "provider";

	String EXERTION_PROVIDER = "exertion" + CPS + "provider";

	String TRUE = "true";

	String FALSE = "false";

	String NULL = "NULL";

	String SELF_SERVICE = "self/service";

	String ANY = "*"; // used for ExertionEnvelop when setting

	// providername in ServiceMethod

	String NONE = "none";

	String GET = "?";

	/** how long (milliseconds) to wait to discover services */
	long MAX_LOOKUP_WAIT = 6000L;

	// different value_type_code
	int SOC_INTEGER = 0;

	int SOC_DOUBLE = 1;

	int SOC_BOOLEAN = 2;

	int SOC_STRING = 3;

	int SOC_DB_OBJECT = 4;

	int SOC_LONG = 5;

	int SOC_FLOAT = 6;

	// different datatype codes Data_Type_cd
	// the datatype inside a 'DataNode' object can assume any of these values

	int SOC_PRIMITIVE = 10;

	int SOC_DATANODE = 11;

	int SOC_SERIALIZABLE = 12;

	int SOC_CONTEXT_LINK = 13;

	int TABLE_NAME = 1;

	// ContextDomain aspects
	int MODIFY_LEAFNODE = 0;

	int ADD_LEAFNODE = 1;

	/* *********** Static persistence state values ********** */
	/**
	 * The meta data of object has been modified either since creation or
	 * restoring from the data store.
	 */
	int META_MODIFIED = 16;

	/**
	 * The attributes of object has been modified either since creation or
	 * restoring from the data store.
	 */
	int ATTRIBUTE_MODIFIED = 32;

	/**
	 * the different types of tasks that are created
	 */
	String TASK_COMMAND = "Command";

	String TASK_SCRIPT = "Script";

	String TASK_JOB = "Job";

	/*
	 *  access classes.
	 * String PUBLIC = "1"; String
	 * SENSITIVE = "2"; String CONFIDENTIAL = "3"; static
	 * String SECRET = "4";
	 */
	/**
	 * scratch id's for cache server.
	 */
	String SCRATCH_TASKEXERTIONIDS = "taskexertionids";

	String SCRATCH_JOBEXERTIONIDS = "jobexertionids";

	String SCRATCH_METHODIDS = "scratchmethodids";

	String SCRATCH_CONTEXTIDS = "scratchcontextids";

	/**
	 * All result are stored in control context under ctx node.A abuffer node
	 * under which all results attached
	 */
	String CONTEXT_RESULT = "ctx";

	// Serach criteria names fro context, jobs & tasks
	String OBJECT_NAME = "Tag";

	String OBJECT_SCOPE = "ScopeCode";

	String OBJECT_OWNER = "Owner";

	String OBJECT_DOMAIN = "ContextDomain";

	String OBJECT_SUBDOMAIN = "SubDomain";

	int NEW_JOB_EVT = 1;

	int NEW_TASK_EVT = 2;

	int NEW_CONTEXT_EVT = 3;

	int UPDATE_JOB_EVT = 4;

	int UPDATE_TASK_EVT = 5;

	int UPDATE_CONTEXT_EVT = 6;

	int DELETE_JOB_EVT = 7;

	int DELETE_TASK_EVT = 8;

	int DELETE_CONTEXT_EVT = 9;

	int PERSISTENCE_EVENT = 10;

	int CATALOGER_EVENT = 11;

	int BROKEN_LINK = 12;

	/**
	 * For Conditional
	 */
	String C_INCREMENT = "in/conditional/increment/";

	String C_DECREMENT = "in/conditional/increment/";

	String PROVIDER_THREAD_GROUP = "sorcer.provider";
	/**
	 * SERVME
	 */
	long LEASE_REFRESH_SERVICER = 60000L;

	long LEASE_SIGNING_SERVICER = 30000L;

	// String ONDEMAND_PROVISIONER_INTERFACE =
	// "AdministrableAutonomicProvisioner";

	long WAIT_TIME_FOR_PROVISIONER = 9000L;

	// various role names
	String ANONYMOUS = "anonymous";
	String ADMIN = "admin";
	String ROOT = "root";
	String APPROVER = "approver";
	String REVIEWER = "reviewer";
	String PUBLISHER = "publisher";
	String LOOKER = "viewer";
	String ORIGINATOR = "originator";
	String LOGGER = "logger";
	String UPDATER = "updater";
	String ALL = "all";
	String SEED = "ac";

	String SYSTEM = "system";
	String SERVLET = "servlet";
	String SYS_LOGIN = "System Login";

	String CUSER = "User";
	String CGROUP = "Group";
	String CROLE = "Role";
	String CPERMISSION = "Permission";
	String CDOCUMENT = "Document";
	String CFOLDER = "Folder";
	String CALL = "All";
	String CEMAIL = "Email";
	String FUPLOAD = "FileUpload";
	String SUPDATE = "ServletUpdate";
	String CAPPROVAL = "Approval";
	String CACL = "ACL";
	String CDRAFT = "Draft";
	String CVERSION = "Document_Version";

	// Basic permissions used in GAPP ACL.
	String CADD = "add", CUPDATE = "update",
			CDELETE = "delete", CREAD = "read", CVIEW = "view";

	String SEP = "|";
	String DELIM = ":";

	// user role codes
	int ANONYMOUS_CD = 1;
	int VIEWER_CD = 2;
	int PUBLISHER_CD = 4;
	int ORIGINATOR_CD = 8;
	int REVIEWER_CD = 16;
	int APPROVER_CD = 32;
	int ADMIN_CD = 64;
	int ALL_CD = 128;
	int ROOT_CD = 256;

	/**
	 * access classes.
	 **/
	String PUBLIC = "1";
	String SENSITIVE = "2";
	String CONFIDENTIAL = "3";
	String SECRET = "4";

	int SEND_MAIL = 181;

	/**
	 * Positions in mail message array
	 */
	int MTO = 0;
	int MCC = 1;
	int MBCC = 2;
	int MSUBJECT = 3;
	int MTEXT = 4;
	int MFROM = 5;
	int MSIZE = 6;

	/**
	 * GApp default strings
	 */
	String OK = "OK";
	String QUIT = "Cancel";
	String CLOSE = "Close";
	String KILL = "Kill";
	String YES = "Yes";
	String NO = "No";
	String SEND = "Send";
	String SEARCH = "Search";
	String CLEAR = "Clear";
	String SET = "Set";
	String VALID = "Valid", INVALID = "Invalid";
	String COMPLETE = "Complete",
			INCOMPLETE = "Incomplete";
	String EXPORT_CONTROL = "Export Control",
			ACCESS_CLASS = "Access Class";


    String NAME_DEFAULT="___SORCER_NAME_DEFAULT___";
    String MDC_SORCER_REMOTE_CALL = "SORCER-REMOTE-CALL";
    String MDC_MOGRAM_ID = "mogId";
    String MDC_PROVIDER_ID = "prvId";
    String MDC_PROVIDER_NAME = "prvName";
    String START_PACKAGE = "com.sun.jini.start";
    String DEPLOYMENT_PACKAGE = "sorcer.core.exertion.deployment";
    String REMOTE_LOGGER_INTERFACE = "RemoteLogger";
    String S_SHARED_DIRS_FILE = "sorcer.sharedDirs.file";
	String S_SHARED_DIRS = "sorcer.sharedDirs";
	String P_SCRATCH_TTL = "scratch.ttl";
	// 30 days as seconds
	long SCRATCH_TTL_DEFAULT = TimeUnit.DAYS.toSeconds(30);

}
