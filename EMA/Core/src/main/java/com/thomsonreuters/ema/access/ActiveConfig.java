///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2015. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.access;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.thomsonreuters.ema.access.OmmConsumerConfig.OperationModel;
import com.thomsonreuters.upa.transport.CompressionTypes;
import com.thomsonreuters.upa.transport.ConnectionTypes;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.dictionary.DictionaryRequest;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryRefresh;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryRequest;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginRequest;

abstract class BaseConfig
{
	final static int DEFAULT_ITEM_COUNT_HINT					= 100000;
	final static int DEFAULT_SERVICE_COUNT_HINT				    = 513;
	final static int DEFAULT_MAX_DISPATCH_COUNT_API_THREAD		= 100;
	final static int DEFAULT_MAX_DISPATCH_COUNT_USER_THREAD	    = 100;
	final static int DEFAULT_DISPATCH_TIMEOUT_API_THREAD		= 0;
	final static int DEFAULT_USER_DISPATCH						= OperationModel.API_DISPATCH;
	
	BaseConfig()
	{
		itemCountHint = DEFAULT_ITEM_COUNT_HINT;
		serviceCountHint = DEFAULT_SERVICE_COUNT_HINT;
		dispatchTimeoutApiThread = DEFAULT_DISPATCH_TIMEOUT_API_THREAD;
		maxDispatchCountApiThread = DEFAULT_MAX_DISPATCH_COUNT_API_THREAD;
		maxDispatchCountUserThread = DEFAULT_MAX_DISPATCH_COUNT_USER_THREAD;
		userDispatch = DEFAULT_USER_DISPATCH;
		xmlTraceEnable = ActiveConfig.DEFAULT_XML_TRACE_ENABLE;
		isSetCorrectConfigGroup = ActiveConfig.DEFAULT_SET_CORRECT_CONFIG_GROUP;
	}
	
	void clear()
	{
		itemCountHint = DEFAULT_ITEM_COUNT_HINT;
		serviceCountHint = DEFAULT_SERVICE_COUNT_HINT;
		dispatchTimeoutApiThread = DEFAULT_DISPATCH_TIMEOUT_API_THREAD;
		maxDispatchCountApiThread = DEFAULT_MAX_DISPATCH_COUNT_API_THREAD;
		maxDispatchCountUserThread = DEFAULT_MAX_DISPATCH_COUNT_USER_THREAD;
		userDispatch = DEFAULT_USER_DISPATCH;
		configuredName = null;
		instanceName = null;
		xmlTraceEnable = ActiveConfig.DEFAULT_XML_TRACE_ENABLE;
		isSetCorrectConfigGroup = ActiveConfig.DEFAULT_SET_CORRECT_CONFIG_GROUP;
	}
	
	String					configuredName;
	String      			instanceName;
	int						itemCountHint;
	int		    			serviceCountHint;
	int						dispatchTimeoutApiThread;
	int						maxDispatchCountApiThread;
	int						maxDispatchCountUserThread;
	int		    			userDispatch;
	boolean 			xmlTraceEnable;
	
	/*ReconnectAttemptLimit,ReconnectMinDelay,ReconnectMaxDelay,MsgKeyInUpdates,XmlTrace... is per Consumer, or per NIProvider
	 *or per IProvider instance now. The per channel configuration on these parameters has been deprecated. This variable is 
	 *used for handling deprecation cases.
	 *True   -- mean there are one or more of  these parameters set in per Consumer, per NIProvider, or per IProvider instance.
	 *False  -- mean there is none of them set in per Consumer, per NIProvider, or per IProvider instance.
	 */
	boolean            	isSetCorrectConfigGroup; 
}

abstract class ActiveConfig extends BaseConfig
{
	final static int DEFAULT_COMPRESSION_THRESHOLD				= 30;
	final static int DEFAULT_COMPRESSION_TYPE					= CompressionTypes.NONE;
	final static int DEFAULT_CONNECTION_TYPE					= ConnectionTypes.SOCKET;
	final static int DEFAULT_CONNECTION_PINGTIMEOUT				= 30000;
	final static int DEFAULT_DICTIONARY_REQUEST_TIMEOUT			= 45000;
	final static int DEFAULT_DIRECTORY_REQUEST_TIMEOUT			= 45000;
	final static int DEFAULT_GUARANTEED_OUTPUT_BUFFERS			= 100;
	final static int DEFAULT_NUM_INPUT_BUFFERS					= 10;
	final static int DEFAULT_SYS_SEND_BUFFER_SIZE				= 0;
	final static int DEFAULT_SYS_RECEIVE_BUFFER_SIZE			= 0;
	final static int DEFAULT_HIGH_WATER_MARK					= 0;
	final static boolean DEFAULT_HANDLE_EXCEPTION				= true;
	final static String DEFAULT_HOST_NAME						= "localhost";
	final static String DEFAULT_CHANNEL_SET_NAME				= ""; 
	final static boolean DEFAULT_INCLUDE_DATE_IN_LOGGER_OUTPUT	= false;
	final static String DEFAULT_INTERFACE_NAME					= "" ;
	final static int DEFAULT_LOGIN_REQUEST_TIMEOUT              = 45000;
	final static int DEFAULT_MAX_OUTSTANDING_POSTS				= 100000;
	final static boolean DEFAULT_MSGKEYINUPDATES				= true;
	final static int DEFAULT_OBEY_OPEN_WINDOW					= 1;
	final static int DEFAULT_PIPE_PORT							= 9001;
	final static int DEFAULT_POST_ACK_TIMEOUT					= 15000;
	final static int DEFAULT_REACTOR_EVENTFD_PORT				= 55000;
	final static int DEFAULT_RECONNECT_ATTEMPT_LIMIT			= -1;
	final static int DEFAULT_RECONNECT_MAX_DELAY				= 5000;
	final static int DEFAULT_RECONNECT_MIN_DELAY				= 1000;
	final static int DEFAULT_REQUEST_TIMEOUT					= 15000;
	final static String DEFAULT_OBJECT_NAME						= "";
	final static boolean DEFAULT_TCP_NODELAY					= true;
	final static String DEFAULT_CONS_MCAST_CFGSTRING			= "";
	final static int DEFAULT_PACKET_TTL							= 5;
	final static int DEFAULT_NDATA								= 7;
	final static int DEFAULT_NMISSING							= 128;
	final static int DEFAULT_NREQ								= 3;
	final static int DEFAULT_PKT_POOLLIMIT_HIGH					= 190000;
	final static int DEFAULT_PKT_POOLLIMIT_LOW					= 180000;
	final static int DEFAULT_TDATA								= 1;
	final static int DEFAULT_TRREQ								= 4;
	final static int DEFAULT_TWAIT								= 3;
	final static int DEFAULT_TBCHOLD							= 3;
	final static int DEFAULT_TPPHOLD							= 3;
	final static int DEFAULT_USER_QLIMIT						= 65535;
	final static boolean DEFAULT_XML_TRACE_ENABLE				= false;
	final static boolean DEFAULT_DIRECT_SOCKET_WRITE			= false;
	final static boolean DEFAULT_SET_CORRECT_CONFIG_GROUP 			= false;
	
	int						obeyOpenWindow;
	int						requestTimeout;
	int						postAckTimeout;
	int						maxOutstandingPosts;
	int						loginRequestTimeOut;
	int						directoryRequestTimeOut;
	int						dictionaryRequestTimeOut;
	List<ChannelConfig>		channelConfigSet;
	LoginRequest			rsslRDMLoginRequest;
	DirectoryRequest		rsslDirectoryRequest;
	DirectoryRefresh		rsslDirectoryRefresh;
	DictionaryRequest		rsslFldDictRequest;
	DictionaryRequest		rsslEnumDictRequest;
	String 					fldDictReqServiceName;
	String 					enumDictReqServiceName;
	DictionaryConfig		dictionaryConfig;
	static String		    defaultServiceName;
	int					    reconnectAttemptLimit;
	int						reconnectMinDelay;
	int						reconnectMaxDelay;
	boolean 				msgKeyInUpdates;
	
	ActiveConfig(String defaultServiceName)
	{
		super();
		
		 obeyOpenWindow = DEFAULT_OBEY_OPEN_WINDOW;
		 requestTimeout = DEFAULT_REQUEST_TIMEOUT;
		 postAckTimeout = DEFAULT_POST_ACK_TIMEOUT;
		 maxOutstandingPosts = DEFAULT_MAX_OUTSTANDING_POSTS;
		 loginRequestTimeOut = DEFAULT_LOGIN_REQUEST_TIMEOUT;
		 directoryRequestTimeOut = DEFAULT_DIRECTORY_REQUEST_TIMEOUT;
		 dictionaryRequestTimeOut = DEFAULT_DICTIONARY_REQUEST_TIMEOUT;
		 userDispatch = DEFAULT_USER_DISPATCH;
		 reconnectAttemptLimit = ActiveConfig.DEFAULT_RECONNECT_ATTEMPT_LIMIT;
		 reconnectMinDelay = ActiveConfig.DEFAULT_RECONNECT_MIN_DELAY;
		 reconnectMaxDelay = ActiveConfig.DEFAULT_RECONNECT_MAX_DELAY;
		 msgKeyInUpdates = ActiveConfig.DEFAULT_MSGKEYINUPDATES ;
		 ActiveConfig.defaultServiceName = defaultServiceName;
		 channelConfigSet = new ArrayList<>();
	}

	void clear()
	{
		super.clear();
		
		obeyOpenWindow = DEFAULT_OBEY_OPEN_WINDOW;
		requestTimeout = DEFAULT_REQUEST_TIMEOUT;
		postAckTimeout = DEFAULT_POST_ACK_TIMEOUT;
		maxOutstandingPosts = DEFAULT_MAX_OUTSTANDING_POSTS;
		userDispatch = DEFAULT_USER_DISPATCH;
		reconnectAttemptLimit = ActiveConfig.DEFAULT_RECONNECT_ATTEMPT_LIMIT;
		reconnectMinDelay = ActiveConfig.DEFAULT_RECONNECT_MIN_DELAY;
		reconnectMaxDelay = ActiveConfig.DEFAULT_RECONNECT_MAX_DELAY;
		msgKeyInUpdates = ActiveConfig.DEFAULT_MSGKEYINUPDATES ;
		dictionaryConfig.clear();

		rsslRDMLoginRequest = null;
		rsslDirectoryRequest = null;
		rsslFldDictRequest = null;
		rsslEnumDictRequest = null;
	}
}

abstract class ActiveServerConfig extends BaseConfig
{
	final static boolean DEFAULT_ACCEPT_MSG_WITHOUT_ACCEPTING_REQUESTS   = false;
	final static boolean DEFAULT_ACCEPT_DIR_MSG_WITHOUT_MIN_FILTERS       = false;
	final static boolean DEFAULT_ACCEPT_MSG_WITHOUT_BEING_LOGIN        = false;
	final static boolean DEFAULT_ACCEPT_MSG_SAMEKEY_BUT_DIFF_STREAM            = false;
	final static boolean DEFAULT_ACCEPT_MSG_THAT_CHANGES_SERVICE    = false;
	final static boolean DEFAULT_ACCEPT_MSG_WITHOUT_QOS_IN_RANGE          = false;
	final static int DEFAULT_CONNECTION_PINGTIMEOUT				  = 60000;
	final static int DEFAULT_CONNECTION_MINPINGTIMEOUT            = 20000;
	final static int DEFAULT_SERVER_SYS_SEND_BUFFER_SIZE		  = 65535;
	final static int DEFAULT_SERVER_SYS_RECEIVE_BUFFER_SIZE		  = 65535;
	
	ServerConfig                           serverConfig;
	static String                          defaultServiceName;
	int                                    operationModel;
	boolean                                acceptMessageWithoutAcceptingRequests;
	boolean                                acceptDirMessageWithoutMinFilters;
	boolean                                acceptMessageWithoutBeingLogin;
	boolean                                acceptMessageSameKeyButDiffStream;
	boolean                                acceptMessageThatChangesService;
	boolean                                acceptMessageWithoutQosInRange;
	
	private LongObject							         serviceId = new LongObject();
	private HashMap<LongObject, ServiceDictionaryConfig> serviceDictionaryConfigMap;

	ActiveServerConfig(String defaultServiceName)
	{
		super();
		ActiveServerConfig.defaultServiceName = defaultServiceName;
		serviceDictionaryConfigMap = new HashMap<>();
		
		acceptMessageWithoutAcceptingRequests = DEFAULT_ACCEPT_MSG_WITHOUT_ACCEPTING_REQUESTS;
		acceptDirMessageWithoutMinFilters = DEFAULT_ACCEPT_DIR_MSG_WITHOUT_MIN_FILTERS;
		acceptMessageWithoutBeingLogin = DEFAULT_ACCEPT_MSG_WITHOUT_BEING_LOGIN;
		acceptMessageSameKeyButDiffStream = DEFAULT_ACCEPT_MSG_SAMEKEY_BUT_DIFF_STREAM;
		acceptMessageThatChangesService = DEFAULT_ACCEPT_MSG_THAT_CHANGES_SERVICE;
		acceptMessageWithoutQosInRange = DEFAULT_ACCEPT_MSG_WITHOUT_QOS_IN_RANGE;
	}
	
	void clear()
	{
		super.clear();
		serviceDictionaryConfigMap.clear();
	}
	
	abstract int dictionaryAdminControl();
	abstract int directoryAdminControl();
	
	ServiceDictionaryConfig getServiceDictionaryConfig(int id)
	{
		return serviceDictionaryConfigMap.get(serviceId.value(id));
	}
	
	void addServiceDictionaryConfig(int id, ServiceDictionaryConfig serviceDictionaryConfig)
	{
		serviceDictionaryConfigMap.put( new LongObject().value(id) , serviceDictionaryConfig);
	}

	void removeServiceDictionaryConfig(int id)
	{
		serviceDictionaryConfigMap.remove(serviceId.value(id));
	}
	
	Collection<ServiceDictionaryConfig> getServiceDictionaryConfigCollection()
	{
		return serviceDictionaryConfigMap.values();
	}
	
	void setServiceDictionaryConfigCollection(Collection<ServiceDictionaryConfig> serviceDictionaryConfigCollection)
	{
		Iterator<ServiceDictionaryConfig> iterator = serviceDictionaryConfigCollection.iterator();
		
		while(iterator.hasNext())
		{
			ServiceDictionaryConfig serviceDictionaryConfig = iterator.next();
			
			if( ( serviceDictionaryConfig.dictionaryProvidedList.size() != 0 ) || ( serviceDictionaryConfig.dictionaryUsedList.size() != 0 ) )
			serviceDictionaryConfigMap.put( new LongObject().value(serviceDictionaryConfig.serviceId), serviceDictionaryConfig);
		}
	}
}

class ChannelConfig
{
	String				name;
	String				interfaceName;
	int					compressionType;
	int					compressionThreshold;
	int					rsslConnectionType;
	int					connectionPingTimeout;
	int					guaranteedOutputBuffers;
	int					numInputBuffers;
	int					sysRecvBufSize;
	int					sysSendBufSize;
	int 				highWaterMark;
	ChannelInfo			channelInfo;

	ChannelConfig() 
	{
		clear();	 
	}
	
	void clear() 
	{
		interfaceName =  ActiveConfig.DEFAULT_INTERFACE_NAME;
		compressionType = ActiveConfig.DEFAULT_COMPRESSION_TYPE;
		compressionThreshold = ActiveConfig.DEFAULT_COMPRESSION_THRESHOLD;
		connectionPingTimeout = ActiveConfig.DEFAULT_CONNECTION_PINGTIMEOUT;
		guaranteedOutputBuffers = ActiveConfig.DEFAULT_GUARANTEED_OUTPUT_BUFFERS;
		numInputBuffers = ActiveConfig.DEFAULT_NUM_INPUT_BUFFERS;
		sysSendBufSize = ActiveConfig.DEFAULT_SYS_SEND_BUFFER_SIZE;
		sysRecvBufSize = ActiveConfig.DEFAULT_SYS_RECEIVE_BUFFER_SIZE;
		highWaterMark = ActiveConfig.DEFAULT_HIGH_WATER_MARK;
		rsslConnectionType = ActiveConfig.DEFAULT_CONNECTION_TYPE;	
	}

	void guaranteedOutputBuffers(long value) { }
	void numInputBuffers(long value) { }
	void reconnectAttemptLimit(long value) { }
	void reconnectMinDelay(long value) { }
	void reconnectMaxDelay(long value) { }
}

class ServerConfig
{
	String				name;
	String				interfaceName;
	int					compressionType;
	int					compressionThreshold;
	int					rsslConnectionType;
	int					connectionPingTimeout;
	int					guaranteedOutputBuffers;
	int					numInputBuffers;
	int					sysRecvBufSize;
	int					sysSendBufSize;
	int 				highWaterMark;
	int                connectionMinPingTimeout;
	
	ServerConfig()
	{
		clear();
	}
	
	void clear()
	{
		interfaceName =  ActiveConfig.DEFAULT_INTERFACE_NAME;
		compressionType = ActiveConfig.DEFAULT_COMPRESSION_TYPE;
		compressionThreshold = ActiveConfig.DEFAULT_COMPRESSION_THRESHOLD;
		guaranteedOutputBuffers = ActiveConfig.DEFAULT_GUARANTEED_OUTPUT_BUFFERS;
		numInputBuffers = ActiveConfig.DEFAULT_NUM_INPUT_BUFFERS;
		sysSendBufSize = ActiveServerConfig.DEFAULT_SERVER_SYS_SEND_BUFFER_SIZE;
		sysRecvBufSize = ActiveServerConfig.DEFAULT_SERVER_SYS_RECEIVE_BUFFER_SIZE;
		highWaterMark = ActiveConfig.DEFAULT_HIGH_WATER_MARK;
		rsslConnectionType = ActiveConfig.DEFAULT_CONNECTION_TYPE;
		
		connectionPingTimeout = ActiveServerConfig.DEFAULT_CONNECTION_PINGTIMEOUT;
		connectionMinPingTimeout = ActiveServerConfig.DEFAULT_CONNECTION_MINPINGTIMEOUT;
	}
	
	void guaranteedOutputBuffers(long value) { }
	void numInputBuffers(long value) { }
}

class SocketChannelConfig extends ChannelConfig
{
	String				hostName;
	String				serviceName;
	boolean				tcpNodelay;
	boolean				directWrite;
	
	SocketChannelConfig() 
	{
		 clear();
	}

	@Override
	void clear() 
	{
		super.clear();
		
		rsslConnectionType = ConnectionTypes.SOCKET;	
		hostName = ActiveConfig.DEFAULT_HOST_NAME;
		serviceName = ActiveConfig.defaultServiceName;
		tcpNodelay = ActiveConfig.DEFAULT_TCP_NODELAY;
		directWrite = ActiveConfig.DEFAULT_DIRECT_SOCKET_WRITE;
	}
}

class SocketServerConfig extends ServerConfig
{
	String serviceName;
	boolean tcpNodelay;
	boolean directWrite;
	
	SocketServerConfig()
	{
		clear();
	}
	
	@Override
	void clear() 
	{
		super.clear();
		
		rsslConnectionType = ConnectionTypes.SOCKET;	
		serviceName = ActiveServerConfig.defaultServiceName;
		tcpNodelay = ActiveConfig.DEFAULT_TCP_NODELAY;
		directWrite = ActiveConfig.DEFAULT_DIRECT_SOCKET_WRITE;
	}
	
}

class ReliableMcastChannelConfig extends ChannelConfig
{
	String				recvAddress;
	String				recvServiceName;
	String				unicastServiceName;
	String				sendAddress;
	String				sendServiceName;
	String				tcpControlPort;
	String				hsmInterface;
	String				hsmMultAddress;
	String				hsmPort;
	int					hsmInterval;
	boolean				disconnectOnGap;
	int					packetTTL;
	int					ndata;
	int					nmissing;
	int					nrreq;
	int					tdata;
	int					trreq;
	int					twait;
	int					tbchold;
	int					tpphold;
	int					pktPoolLimitHigh;
	int					pktPoolLimitLow;
	int					userQLimit;

	ReliableMcastChannelConfig()
	{
		clear();
	}

	@Override
	void clear()
	{
		super.clear();
		
		rsslConnectionType = ConnectionTypes.RELIABLE_MCAST;
		recvAddress = ActiveConfig.DEFAULT_CONS_MCAST_CFGSTRING;
		recvServiceName = ActiveConfig.DEFAULT_CONS_MCAST_CFGSTRING;
		unicastServiceName = ActiveConfig.DEFAULT_CONS_MCAST_CFGSTRING;
		sendAddress = ActiveConfig.DEFAULT_CONS_MCAST_CFGSTRING;
		sendServiceName = ActiveConfig.DEFAULT_CONS_MCAST_CFGSTRING;
		hsmInterface = ActiveConfig.DEFAULT_CONS_MCAST_CFGSTRING;
		tcpControlPort = ActiveConfig.DEFAULT_CONS_MCAST_CFGSTRING;
		hsmMultAddress = ActiveConfig.DEFAULT_CONS_MCAST_CFGSTRING;
		hsmPort = ActiveConfig.DEFAULT_CONS_MCAST_CFGSTRING;
		hsmInterval = 0;
		packetTTL	= ActiveConfig.DEFAULT_PACKET_TTL;
	    ndata = ActiveConfig.DEFAULT_NDATA;
	    nmissing = ActiveConfig.DEFAULT_NMISSING;
	    nrreq = ActiveConfig.DEFAULT_NREQ;
	    pktPoolLimitHigh = ActiveConfig.DEFAULT_PKT_POOLLIMIT_HIGH;
	    pktPoolLimitLow = ActiveConfig.DEFAULT_PKT_POOLLIMIT_LOW;
	    tdata = ActiveConfig.DEFAULT_TDATA;
	    trreq = ActiveConfig.DEFAULT_TRREQ;
	    twait = ActiveConfig.DEFAULT_TWAIT;
	    tbchold = ActiveConfig.DEFAULT_TBCHOLD;
	    tpphold = ActiveConfig.DEFAULT_TPPHOLD;
	    userQLimit = ActiveConfig.DEFAULT_USER_QLIMIT;
		disconnectOnGap = false;
	}
}

class EncryptedChannelConfig extends ChannelConfig
{
	String				hostName;
	String				serviceName;
	String				objectName;
	Boolean				tcpNodelay;

	EncryptedChannelConfig()
	{
		clear();
	}

	@Override
	void clear() 
	{
		super.clear();
		
		rsslConnectionType = ConnectionTypes.ENCRYPTED;
		hostName = ActiveConfig.DEFAULT_HOST_NAME;
		serviceName = ActiveConfig.defaultServiceName;
		tcpNodelay = ActiveConfig.DEFAULT_TCP_NODELAY;
		objectName = ActiveConfig.DEFAULT_OBJECT_NAME;
	}
}

class HttpChannelConfig extends ChannelConfig
{
	String			hostName;
	String			serviceName;
	String			objectName;
	Boolean			tcpNodelay;

	HttpChannelConfig() 
	{
		clear();
	}

	@Override
	void clear()
	{
		super.clear();
		
		rsslConnectionType = ConnectionTypes.HTTP;
		hostName = ActiveConfig.DEFAULT_HOST_NAME;
		serviceName = ActiveConfig.defaultServiceName;
		tcpNodelay = ActiveConfig.DEFAULT_TCP_NODELAY;
		objectName = ActiveConfig.DEFAULT_OBJECT_NAME;
	}
}

class DictionaryConfig
{
	String		dictionaryName;
	String		rdmfieldDictionaryFileName;
	String		enumtypeDefFileName;
	String		rdmFieldDictionaryItemName;
	String		enumTypeDefItemName;
	boolean     isLocalDictionary;

	DictionaryConfig(boolean localDictionary)
	{
		isLocalDictionary = localDictionary;
	}

	void clear()
	{
		dictionaryName = null;
		rdmfieldDictionaryFileName = null;
		enumtypeDefFileName = null;
	}
}

class ServiceDictionaryConfig
{
	int						serviceId;
	List<DictionaryConfig>	dictionaryUsedList;
	List<DictionaryConfig>	dictionaryProvidedList;
	
	ServiceDictionaryConfig()
	{
		dictionaryUsedList = new ArrayList<>();
		dictionaryProvidedList = new ArrayList<>();
	}
	
	void clear()
	{
		dictionaryUsedList.clear();
		dictionaryProvidedList.clear();
	}
}
