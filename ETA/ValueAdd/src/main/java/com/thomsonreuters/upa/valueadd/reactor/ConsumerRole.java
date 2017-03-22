package com.thomsonreuters.upa.valueadd.reactor;

import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.rdm.Dictionary;
import com.thomsonreuters.upa.rdm.Directory;
import com.thomsonreuters.upa.rdm.Login;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.dictionary.DictionaryClose;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.dictionary.DictionaryMsgFactory;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.dictionary.DictionaryMsgType;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.dictionary.DictionaryRequest;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryClose;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryMsgFactory;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryMsgType;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryRequest;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginClose;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginMsgFactory;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginMsgType;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginRequest;

/**
 * Class representing the role of an OMM Consumer.
 * 
 * @see ReactorRole
 * @see ReactorRoleTypes
 */
public class ConsumerRole extends ReactorRole
{
    LoginRequest _loginRequest = null;
    LoginClose _loginClose = null;
    DirectoryRequest _directoryRequest = null;
    DirectoryClose _directoryClose = null;
    DictionaryRequest _fieldDictionaryRequest = null;
    DictionaryClose _fieldDictionaryClose = null;
    DictionaryRequest _enumDictionaryRequest = null;
    DictionaryClose _enumDictionaryClose = null;
    RDMLoginMsgCallback _loginMsgCallback = null;
    RDMDirectoryMsgCallback _directoryMsgCallback = null;
    RDMDictionaryMsgCallback _dictionaryMsgCallback = null;
    ConsumerWatchlistOptions _consumerWatchlistOptions = null;
    int _dictionaryDownloadMode = DictionaryDownloadModes.NONE;
	Buffer _fieldDictionaryName = CodecFactory.createBuffer();
	Buffer _enumTypeDictionaryName = CodecFactory.createBuffer();
	boolean _receivedFieldDictionaryResp = false;
	boolean _receivedEnumDictionaryResp = false;

    static final int LOGIN_STREAM_ID = 1;
    static final int DIRECTORY_STREAM_ID = 2;
    static final int FIELD_DICTIONARY_STREAM_ID = 3;
    static final int ENUM_DICTIONARY_STREAM_ID = 4;

    static final long FILTER_TO_REQUEST = Directory.ServiceFilterFlags.INFO |
            Directory.ServiceFilterFlags.STATE | Directory.ServiceFilterFlags.GROUP;

    public ConsumerRole()
    {
        _type = ReactorRoleTypes.CONSUMER;
        _fieldDictionaryName.data("RWFFld");
        _enumTypeDictionaryName.data("RWFEnum");
        _consumerWatchlistOptions = new ConsumerWatchlistOptions();
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
     * @return the loginRequest
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
        _loginRequest.role(Login.RoleTypes.CONS);

        return;
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
    
    /** A Directory Request to be sent during the setup of a Consumer-Provider
     * session. This can be populated with a user's specific information or
     * invoke {@link #initDefaultRDMDirectoryRequest()} to populate with default
     * information. Requires LoginRequest to be set.
     * 
     * @param directoryRequest
     */
    public void rdmDirectoryRequest(DirectoryRequest directoryRequest)
    {
        _directoryRequest = directoryRequest;
    }

    /** A Directory Request to be sent during the setup of a Consumer-Provider
     * session. This can be populated with a user's specific information or
     * invoke {@link #initDefaultRDMDirectoryRequest()} to populate with default
     * information. Requires LoginRequest to be set.
     * 
     * @return the directoryRequest
     */
    public DirectoryRequest rdmDirectoryRequest()
    {
        return _directoryRequest;
    }

    /**
     * Initializes the RDM DirectoryRequest with default information. If the
     * rdmDirectoryRequest has already been defined (due to a previous call to
     * {@link #rdmDirectoryRequest(DirectoryRequest)}) the rdmDirectoryRequest
     * object will be reused.
     */
    public void initDefaultRDMDirectoryRequest()
    {
    	int streamId;
    	
        if (_directoryRequest == null)
        {
        	streamId = DIRECTORY_STREAM_ID;
            _directoryRequest = (DirectoryRequest)DirectoryMsgFactory.createMsg();
        }
        else
        {
        	streamId = (_directoryRequest.streamId() == 0 ? DIRECTORY_STREAM_ID : _directoryRequest.streamId());
            _directoryRequest.clear();
        }
        
        _directoryRequest.rdmMsgType(DirectoryMsgType.REQUEST);
        _directoryRequest.streamId(streamId);
        _directoryRequest.filter(FILTER_TO_REQUEST);
        _directoryRequest.applyStreaming();
        
        return;
    }
    
    /*
     * The DirectoryClose to be sent to close the Directory stream.
     * This corresponds to the DirectoryRequest sent during the
     * connection establishment process.
     */
    DirectoryClose rdmDirectoryClose()
    {
    	if (_directoryRequest == null)
    		return null;
    	
    	if (_directoryClose == null)
    	{
    		_directoryClose = (DirectoryClose)DirectoryMsgFactory.createMsg();
    		_directoryClose.rdmMsgType(DirectoryMsgType.CLOSE);
    	}
    	
    	_directoryClose.streamId(_directoryRequest.streamId());
    	
        return _directoryClose;
    }

    Buffer fieldDictionaryName()
    {
        return _fieldDictionaryName;
    }

    /* A Field Dictionary Request to be sent during the setup of a Consumer-Provider
     * session. Requires DirectoryRequest to be set.
     */
    void rdmFieldDictionaryRequest(DictionaryRequest fieldDictionaryRequest)
    {
    	_fieldDictionaryRequest = fieldDictionaryRequest;
    }

    /* A Field Dictionary Request to be sent during the setup of a Consumer-Provider
     * session. Requires DirectoryRequest to be set.
     */
    DictionaryRequest rdmFieldDictionaryRequest()
    {
        return _fieldDictionaryRequest;
    }

    /*
     * Initializes the RDM Field DictionaryRequest with default information. If the
     * rdmFieldDictionaryRequest has already been defined, the rdmFieldDictionaryRequest
     * object will be reused.
     */
    void initDefaultRDMFieldDictionaryRequest()
    {
    	int streamId;
    	
        if (_fieldDictionaryRequest == null)
        {
        	streamId = FIELD_DICTIONARY_STREAM_ID;
        	_fieldDictionaryRequest = (DictionaryRequest)DictionaryMsgFactory.createMsg();
        }
        else
        {
        	streamId = (_fieldDictionaryRequest.streamId() == 0 ? FIELD_DICTIONARY_STREAM_ID : _fieldDictionaryRequest.streamId());
        	_fieldDictionaryRequest.clear();
        }
        
        // make sure stream id isn't already being used
    	while (streamId == _loginRequest.streamId() ||
     		   streamId == _directoryRequest.streamId())
     	{
     		streamId++;
     	}
        _fieldDictionaryRequest.rdmMsgType(DictionaryMsgType.REQUEST);
        _fieldDictionaryRequest.streamId(streamId);
        _fieldDictionaryRequest.applyStreaming();
        _fieldDictionaryRequest.verbosity(Dictionary.VerbosityValues.NORMAL);
        _fieldDictionaryRequest.dictionaryName(_fieldDictionaryName);
        
        return;
    }

    /*
     * The DictionaryClose to be sent to close the Field Dictionary stream.
     * This corresponds to the Field DictionaryRequest sent during the
     * connection establishment process.
     */
    DictionaryClose rdmFieldDictionaryClose()
    {
    	if (_fieldDictionaryRequest == null)
    		return null;
    	
    	if (_fieldDictionaryClose == null)
    	{
    		_fieldDictionaryClose = (DictionaryClose)DictionaryMsgFactory.createMsg();
    		_fieldDictionaryClose.rdmMsgType(DictionaryMsgType.CLOSE);
    	}
    	
    	_fieldDictionaryClose.streamId(_fieldDictionaryRequest.streamId());
    	
        return _fieldDictionaryClose;
    }

    Buffer enumTypeDictionaryName()
    {
        return _enumTypeDictionaryName;
    }

    /* A EnumType Dictionary Request to be sent during the setup of a Consumer-Provider
     * session. Requires Field DictionaryRequest to be set.
     */
    void rdmEnumDictionaryRequest(DictionaryRequest enumDictionaryRequest)
    {
    	_enumDictionaryRequest = enumDictionaryRequest;
    }

    /* A EnumType Dictionary Request to be sent during the setup of a Consumer-Provider
     * session. Requires Field DictionaryRequest to be set.
     */
    DictionaryRequest rdmEnumDictionaryRequest()
    {
        return _enumDictionaryRequest;
    }

    /*
     * Initializes the RDM EnumType DictionaryRequest with default information. If the
     * rdmEnumDictionaryRequest has already been defined, the rdmEnumDictionaryRequest
     * object will be reused.
     */
    void initDefaultRDMEnumDictionaryRequest()
    {
    	int streamId;
    	
        if (_enumDictionaryRequest == null)
        {
        	streamId = ENUM_DICTIONARY_STREAM_ID;
        	_enumDictionaryRequest = (DictionaryRequest)DictionaryMsgFactory.createMsg();
        }
        else
        {
        	streamId = (_enumDictionaryRequest.streamId() == 0 ? ENUM_DICTIONARY_STREAM_ID : _enumDictionaryRequest.streamId());
        	_enumDictionaryRequest.clear();
        }
        
        // make sure stream id isn't already being used
    	while (streamId == _loginRequest.streamId() ||
      		   streamId == _directoryRequest.streamId() ||
      		   streamId == _fieldDictionaryRequest.streamId())
      	{
      		streamId++;
      	}
        _enumDictionaryRequest.rdmMsgType(DictionaryMsgType.REQUEST);
        _enumDictionaryRequest.streamId(streamId);
        _enumDictionaryRequest.applyStreaming();
        _enumDictionaryRequest.verbosity(Dictionary.VerbosityValues.NORMAL);
        _enumDictionaryRequest.dictionaryName(_enumTypeDictionaryName);
        
        return;
    }

    /*
     * The DictionaryClose to be sent to close the EnumType Dictionary stream.
     * This corresponds to the EnumType DictionaryRequest sent during the
     * connection establishment process.
     */
    DictionaryClose rdmEnumDictionaryClose()
    {
    	if (_enumDictionaryRequest == null)
    		return null;
    	
    	if (_enumDictionaryClose == null)
    	{
    		_enumDictionaryClose = (DictionaryClose)DictionaryMsgFactory.createMsg();
    		_enumDictionaryClose.rdmMsgType(DictionaryMsgType.CLOSE);
    	}
    	
    	_enumDictionaryClose.streamId(_enumDictionaryRequest.streamId());
    	
        return _enumDictionaryClose;
    }

    /**
     * Specifies the {@link DictionaryDownloadModes}.
     * 
     * @param mode A specific DictionaryDownloadModes
     * 
     * @see DictionaryDownloadModes
     */
    public void dictionaryDownloadMode(int mode)
    {
        assert(mode == DictionaryDownloadModes.NONE ||
        	mode == DictionaryDownloadModes.FIRST_AVAILABLE);
        _dictionaryDownloadMode = mode;
    }

    /**
     * Specifies the {@link DictionaryDownloadModes}.
     * 
     * @return the dictionaryDownloadMode
     * 
     * @see DictionaryDownloadModes
     */
    public int dictionaryDownloadMode()
    {
        return _dictionaryDownloadMode;
    }
    
    void receivedFieldDictionaryResp(boolean dictionaryReceived)
    {
    	_receivedFieldDictionaryResp = dictionaryReceived;
    }
    
    boolean receivedFieldDictionaryResp()
    {
    	return _receivedFieldDictionaryResp;
    }

    void receivedEnumDictionaryResp(boolean dictionaryReceived)
    {
    	_receivedEnumDictionaryResp = dictionaryReceived;
    }
    
    boolean receivedEnumDictionaryResp()
    {
    	return _receivedEnumDictionaryResp;
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

    /** A callback function for processing RDMDirectoryMsgEvents received. If not present,
     * the received message will be passed to the defaultMsgCallback.
     * 
     * @param callback
     * 
     * @see RDMDirectoryMsgCallback
     * @see RDMDirectoryMsgEvent
     */
    public void directoryMsgCallback(RDMDirectoryMsgCallback callback)
    {
        _directoryMsgCallback = callback;
    }

    /** A callback function for processing RDMDirectoryMsgEvents received. If not present,
     * the received message will be passed to the defaultMsgCallback.
     * 
     * @return the directoryMsgCallback
     */
    public RDMDirectoryMsgCallback directoryMsgCallback()
    {
        return _directoryMsgCallback;
    }

    /** A callback function for processing RDMDictionaryMsgEvents received. If not present,
     * the received message will be passed to the defaultMsgCallback.
     * 
     * @param callback
     * 
     * @see RDMDictionaryMsgCallback
     * @see RDMDictionaryMsgEvent
     */
    public void dictionaryMsgCallback(RDMDictionaryMsgCallback callback)
    {
        _dictionaryMsgCallback = callback;
    }

    /** A callback function for processing RDMDictionaryMsgEvents received. If not present,
     * the received message will be passed to the defaultMsgCallback.
     * 
     * @return the dictionaryMsgCallback
     */
    public RDMDictionaryMsgCallback dictionaryMsgCallback()
    {
        return _dictionaryMsgCallback;
    }

    /** Options for using the watchlist. Use to enable watchlist and
     * set watchlist options for ConsumerRole.
     *
     * @return ConsumerRole watchlist options
     **/
	public ConsumerWatchlistOptions watchlistOptions() 
	{
		return _consumerWatchlistOptions;
	}
}
