package com.thomsonreuters.upa.valueadd.reactor;

import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.codec.DataStates;
import com.thomsonreuters.upa.codec.FilterEntryActions;
import com.thomsonreuters.upa.codec.MapEntryActions;
import com.thomsonreuters.upa.codec.Qos;
import com.thomsonreuters.upa.codec.QosRates;
import com.thomsonreuters.upa.codec.QosTimeliness;
import com.thomsonreuters.upa.codec.StateCodes;
import com.thomsonreuters.upa.codec.StreamStates;
import com.thomsonreuters.upa.rdm.Directory;
import com.thomsonreuters.upa.rdm.DomainTypes;
import com.thomsonreuters.upa.rdm.Login;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryClose;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryMsgFactory;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryMsgType;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryRefresh;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.Service;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginClose;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginMsgFactory;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginMsgType;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginRequest;

/**
 * Class representing the role of an OMM Non-Interactive Provider.
 * 
 * @see ReactorRole
 * @see ReactorRoleTypes
 */
public class NIProviderRole extends ReactorRole
{
    static final int LOGIN_STREAM_ID = 1;
    static final int DIRECTORY_STREAM_ID = -1;
    static final int OPEN_LIMIT = 5;
    static final String VENDOR = "Thomson Reuters";
    static final String LINK_NAME = "NI_PUB";
    static final String FIELD_DICTIONARY_NAME = "RWFFld";
    static final String ENUM_TYPE_DICTIONARY_NAME = "RWFEnum";

    static final long FILTER_TO_REFRESH = Directory.ServiceFilterFlags.INFO
            | Directory.ServiceFilterFlags.STATE | Directory.ServiceFilterFlags.LOAD
            | Directory.ServiceFilterFlags.LINK;

    LoginRequest _loginRequest = null;
    LoginClose _loginClose = null;
    DirectoryRefresh _directoryRefresh = null;
    DirectoryClose _directoryClose = null;
    RDMLoginMsgCallback _loginMsgCallback = null;
	Buffer _stateText = CodecFactory.createBuffer();

    private Service _service = DirectoryMsgFactory.createService();
    
    public NIProviderRole()
    {
        _type = ReactorRoleTypes.NIPROVIDER;
        _stateText.data("Source Directory Refresh Completed");
    }
    
    /**
     * The {@link LoginRequest} to be sent during the connection establishment
     * process. This can be populated with a user's specific information or
     * invoke {@link #initDefaultRDMLoginRequest()} to populate with default
     * information. If this parameter is left empty no login will be sent to
     * the system; useful for systems that do not require a login.
     * 
     * @param loginRequest
     */
    public void rdmLoginRequest(LoginRequest loginRequest)
    {
        _loginRequest = loginRequest;
    }
    
    /**
     * The {@link LoginRequest} to be sent during the connection establishment
     * process. This can be populated with a user's specific information or
     * invoke {@link #initDefaultRDMLoginRequest()} to populate with default
     * information. If this parameter is left empty no login will be sent to
     * the system; useful for systems that do not require a login.
     * 
     * @return the rdmLoginRequest
     */
    public LoginRequest rdmLoginRequest()
    {
        return _loginRequest;
    }
    
    /**
     * Initializes the RDM LoginRequest with default information. If the
     * rdmLoginRequest has already been defined (due to a previous call to
     * {@link #rdmLoginRequest(LoginRequest)}) the rdmLoginRequest object will
     * be reused.
     */
    public void initDefaultRDMLoginRequest()
    {
    	String userName = "";
    	int streamId;
    	
        if (_loginRequest == null)
        {
        	streamId = LOGIN_STREAM_ID;
            _loginRequest = (LoginRequest)LoginMsgFactory.createMsg();
        }
        else
        {
        	streamId = (_loginRequest.streamId() == 0 ? LOGIN_STREAM_ID : _loginRequest.streamId());
        	userName = _loginRequest.userName().toString();
            _loginRequest.clear();
        }

        _loginRequest.rdmMsgType(LoginMsgType.REQUEST);
        _loginRequest.initDefaultRequest(streamId);
        _loginRequest.applyHasAttrib();
        if (!userName.equals(""))
        {
        	_loginRequest.userName().data(userName);
        }
        _loginRequest.applyHasRole();
        _loginRequest.role(Login.RoleTypes.PROV);
    }
    
    /*
     * The LoginClose to be sent to close the Login stream.
     * This corresponds to the LoginRequest sent during the
     * connection establishment process.
     */
    LoginClose rdmLoginClose()
    {
    	if (_loginRequest == null)
    		return null;
    	
    	if (_loginClose == null)
    	{
    		_loginClose = (LoginClose)LoginMsgFactory.createMsg();
    		_loginClose.rdmMsgType(LoginMsgType.CLOSE);
    	}
    	
    	_loginClose.streamId(_loginRequest.streamId());
    	
        return _loginClose;
    }
    
    /** A Directory Refresh to be sent during the setup of a Non-Interactive Provider
     * session. This can be populated with a user's specific information or
     * invoke {@link #initDefaultRDMDirectoryRefresh(String, int)} to populate with default
     * information. Requires LoginRequest to be set.
     * 
     * @param directoryRefresh
     */
    public void rdmDirectoryRefresh(DirectoryRefresh directoryRefresh)
    {
        _directoryRefresh = directoryRefresh;
    }
    
    /** A Directory Refresh to be sent during the setup of a Non-Interactive Provider
     * session. This can be populated with a user's specific information or
     * invoke {@link #initDefaultRDMDirectoryRefresh(String, int)} to populate with default
     * information. Requires LoginRequest to be set.
     * 
     * @return the rdmDirectoryRefresh
     */
    public DirectoryRefresh rdmDirectoryRefresh()
    {
        return _directoryRefresh;
    }
    
    /**
     * Initializes the RDM DirectoryRefresh with default information. If the
     * rdmDirectoryRefresh has already been defined (due to a previous call to
     * {@link #rdmDirectoryRefresh(DirectoryRefresh)}) the rdmDirectoryRefresh
     * object will be reused.
     * 
     * @param serviceName the serviceName for this source directory refresh
     * @param serviceId the serviceId for this source directory refresh
     */
    public void initDefaultRDMDirectoryRefresh(String serviceName, int serviceId)
    {
    	int streamId;
    	
        if (_directoryRefresh == null)
        {
        	streamId = DIRECTORY_STREAM_ID;
            _directoryRefresh = (DirectoryRefresh)DirectoryMsgFactory.createMsg();
        }
        else
        {
        	streamId = (_directoryRefresh.streamId() == 0 ? DIRECTORY_STREAM_ID : _directoryRefresh.streamId());
            _directoryRefresh.clear();
        }

        _directoryRefresh.rdmMsgType(DirectoryMsgType.REFRESH);
        
        // stream id
        _directoryRefresh.streamId(streamId);
        
        // state information
        _directoryRefresh.state().streamState(StreamStates.OPEN);
        _directoryRefresh.state().dataState(DataStates.OK);
        _directoryRefresh.state().code(StateCodes.NONE);
        _directoryRefresh.state().text(_stateText);
        
        //clear cache
        _directoryRefresh.applyClearCache();

        //attribInfo information
        _directoryRefresh.filter(FILTER_TO_REFRESH);

        //_service
        _service.clear();
        _service.action(MapEntryActions.ADD);

        //set the _service Id (map key)
        _service.serviceId(serviceId);

        if ((FILTER_TO_REFRESH & Directory.ServiceFilterFlags.INFO) != 0)
        {
            _service.applyHasInfo();
            _service.info().action(FilterEntryActions.SET);

            //vendor 
            _service.info().applyHasVendor();
            _service.info().vendor().data(VENDOR);

            //_service name - required
            _service.info().serviceName().data(serviceName);

            //Qos Range is not supported
            _service.info().applyHasSupportsQosRange();
            _service.info().supportsQosRange(0);
          
            //capabilities - required
            _service.info().capabilitiesList().add((long)DomainTypes.MARKET_PRICE);
            _service.info().capabilitiesList().add((long)DomainTypes.MARKET_BY_ORDER);

            //qos
            _service.info().applyHasQos();
            Qos qos = CodecFactory.createQos();
            qos.rate(QosRates.TICK_BY_TICK);
            qos.timeliness(QosTimeliness.REALTIME);
            _service.info().qosList().add(qos);

            //dictionary used
            _service.info().applyHasDictionariesUsed();
            _service.info().dictionariesUsedList().add(FIELD_DICTIONARY_NAME);
            _service.info().dictionariesUsedList().add(ENUM_TYPE_DICTIONARY_NAME);

            //isSource = Service is provided directly from original publisher
            _service.info().applyHasIsSource();
            _service.info().isSource(1);
            
            /*
             * itemList - Name of SymbolList that includes all of the items that
             * the publisher currently provides. Blank for this example
             */
            _service.info().applyHasItemList();
            _service.info().itemList().data("");

            _service.info().applyHasAcceptingConsumerStatus();
            //accepting customer status = no
            _service.info().acceptingConsumerStatus(0);

            _service.info().applyHasSupportsOutOfBandSnapshots();
            //supports out of band snapshots = no
            _service.info().supportsOutOfBandSnapshots(0);
        }

        if ((FILTER_TO_REFRESH & Directory.ServiceFilterFlags.STATE) != 0)
        {
            _service.applyHasState();
            _service.state().action(FilterEntryActions.SET);

            //_service state
            _service.state().serviceState(1);

            //accepting requests
            _service.state().applyHasAcceptingRequests();
            _service.state().acceptingRequests(1);

            //status
            _service.state().applyHasStatus();
            _service.state().status().dataState(DataStates.OK);
            _service.state().status().streamState(StreamStates.OPEN);
            _service.state().status().code(StateCodes.NONE);
            _service.state().status().text().data("OK");
        }

        if ((FILTER_TO_REFRESH & Directory.ServiceFilterFlags.LOAD) != 0)
        {
            _service.applyHasLoad();
            _service.load().action(FilterEntryActions.SET);

            //open limit
            _service.load().applyHasOpenLimit();
            _service.load().openLimit(OPEN_LIMIT);

            //load factor
            _service.load().applyHasLoadFactor();
            _service.load().loadFactor(1);
        }

        if ((FILTER_TO_REFRESH & Directory.ServiceFilterFlags.LINK) != 0)
        {
            _service.applyHasLink();
            _service.link().action(FilterEntryActions.SET);

            Service.ServiceLink serviceLink = new Service.ServiceLink();

            //link name - Map Entry Key
            serviceLink.name().data(LINK_NAME);

            //link type
            serviceLink.applyHasType();
            serviceLink.type(Directory.LinkTypes.INTERACTIVE);

            //link state
            serviceLink.linkState(Directory.LinkStates.UP);

            //link code
            serviceLink.applyHasCode();
            serviceLink.linkCode(Directory.LinkCodes.OK);

            //link text
            serviceLink.applyHasText();
            serviceLink.text().data("Link state is up");

            _service.link().linkList().add(serviceLink);
        }

        _directoryRefresh.serviceList().add(_service);
    }
    
    /*
     * The DirectoryClose to be sent to close the Directory stream.
     * This corresponds to the DirectoryRefresh sent during the
     * connection establishment process.
     */
    DirectoryClose rdmDirectoryClose()
    {
        if (_directoryRefresh == null)
            return null;
        
        if (_directoryClose == null)
        {
            _directoryClose = (DirectoryClose)DirectoryMsgFactory.createMsg();
            _directoryClose.rdmMsgType(DirectoryMsgType.CLOSE);
        }
        
        _directoryClose.streamId(_directoryRefresh.streamId());
        
        return _directoryClose;
    }
    
    /** A callback function for processing RDMLoginMsgEvents received. If not present,
     * the received message will be passed to the defaultMsgCallback.
     * 
     * @param callback
     * 
     * @see RDMLoginMsgCallback
     * @see RDMLoginMsgEvent
     */
    public void loginMsgCallback(RDMLoginMsgCallback callback)
    {
        _loginMsgCallback = callback;
    }

    /** A callback function for processing RDMLoginMsgEvents received. If not present,
     * the received message will be passed to the defaultMsgCallback.
     * 
     * @return the loginMsgCallback
     */
    public RDMLoginMsgCallback loginMsgCallback()
    {
        return _loginMsgCallback;
    }
}
