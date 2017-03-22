package com.thomsonreuters.upa.examples.provider;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Set;

import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.codec.CodecReturnCodes;
import com.thomsonreuters.upa.codec.DecodeIterator;
import com.thomsonreuters.upa.codec.Msg;
import com.thomsonreuters.upa.shared.ClientSessionInfo;
import com.thomsonreuters.upa.shared.CommandLine;
import com.thomsonreuters.upa.shared.LoginRequestInfo;
import com.thomsonreuters.upa.shared.ProviderDirectoryHandler;
import com.thomsonreuters.upa.shared.ProviderSession;
import com.thomsonreuters.upa.shared.ReceivedMsgCallback;
import com.thomsonreuters.upa.examples.common.ProviderDictionaryHandler;
import com.thomsonreuters.upa.examples.common.ProviderLoginHandler;
import com.thomsonreuters.upa.examples.common.UnSupportedMsgHandler;
import com.thomsonreuters.upa.rdm.DomainTypes;
import com.thomsonreuters.upa.transport.BindOptions;
import com.thomsonreuters.upa.transport.Channel;
import com.thomsonreuters.upa.transport.ChannelState;
import com.thomsonreuters.upa.transport.Error;
import com.thomsonreuters.upa.transport.Server;
import com.thomsonreuters.upa.transport.TransportBuffer;
import com.thomsonreuters.upa.transport.TransportFactory;
import com.thomsonreuters.upa.transport.TransportReturnCodes;

/**
 * This is the main class for the UPA Java Provider application. It is a
 * single-threaded server application. The application uses either the operating
 * parameters entered by the user or a default set of parameters.
 * <p>
 * The purpose of this application is to interactively provide Level I Market Price, 
 * Level II Market By Order, Level II Market By Price, and Symbol List data to 
 * one or more consumers. It allowed, it requests dictionary from the adh.
 * <p>
 * It is a single-threaded server application. First the application initializes 
 * the UPA transport and binds the server. If the dictionary files are in the path
 * it loads dictionary information from the RDMFieldDictionary and enumtype.def files. 
 * Finally, it processes login, source directory, dictionary, market price, 
 * market by order, market by price, and symbol list 
 * requests from consumers and sends the appropriate responses.
 * <p>
 * Level II Market By Price refresh messages are sent as multi-part messages. An 
 * update message is sent between each part of the multi-part refresh message.
 * <p>
 * Dictionary requests are supported by this application. If the Login Request
 * indicates the presence of the dictionary request feature and the dictionaries are
 * not setup on startup, this application sends dictionary requests for both
 * field dictionary and enumtype dictionary. The responses to the dictionary requests
 * are processed as well.
 * <p>
 * Batch requests are supported by this application. The login response message 
 * indicates that batch support is present. Batch requests are accepted and a stream 
 * is opened for each item in the batch request.
 * <p>
 * Posting requests are supported by this application for items that have already 
 * been opened by a consumer. On-stream and off-stream posts are accepted and sent 
 * out to any consumer that has the item open. Off-stream posts for items that 
 * have not already been opened by a consumer are rejected (in this example). 
 * <p>
 * Private stream requests are also supported by this application. All items requested 
 * with the private stream flag set in the request message result in the private 
 * stream flag set in the applicable response messages. If a request is received 
 * without the private stream flag set for the item name of "RES-DS", this application 
 * redirects the consumer to open the "RES-DS" item on a private stream instead 
 * of a normal stream.
 * <p>
 * Symbol List requests are expected to use a symbol list name of "_UPA_ITEM_LIST". 
 * The symbol list name is provided in the source directory response for the consumer 
 * to use.
 * <p> 
 * This class is also a call back for all events from provider. It dispatches
 * events to domain specific handlers.
 * <p>
 * This application is intended as a basic usage example. Some of the design
 * choices were made to favor simplicity and readability over performance. It is
 * not intended to be used for measuring performance. This application uses
 * Value Add and shows how using Value Add simplifies the writing of UPA
 * applications. Because Value Add is a layer on top of UPA, you may see a
 * slight decrease in performance compared to writing applications directly to
 * the UPA interfaces.
 * <p>
 * <H2>Setup Environment</H2>
 * <p>
 * The RDMFieldDictionary and enumtype.def files must be located in the
 * directory of execution. If not available and adh supports dictionary requests,
 * the dictionary is down loaded from adh.
 * <p>
 * <H2>Running the application:</H2>
 * <p>
 * Change directory to the <i>Applications/Examples</i> directory and run <i>ant</i> to
 * build.
 * <p>
 * java -cp ./bin;../../Libs/upaValueAdd.jar;../../Libs/upa.jar
 * com.thomsonreuters.upa.examples.provider.Provider [-p srvrPortNo] [-id
 * Serviceid] [-s serviceName] [-x] [-runtime runTime]
 * <p>
 * <ul>
 * <li>-id Service id. Default is <i>1</i>.
 * <li>-p Server port number. Default is <i>14002</i>.
 * <li>-s Service name. Default is <i>DIRECT_FEED</i>.
 * <li>-x Provides XML tracing of messages.
 * <li>-runtime run time. Default is 1200 seconds. Controls the time the
 * application will run before exiting, in seconds.
 * </ul>
 * 
 * @see ProviderSession
 * @see ProviderDictionaryHandler
 * @see ProviderDirectoryHandler
 * @see ProviderLoginHandler
 * @see ItemHandler
 */
public class Provider implements ReceivedMsgCallback
{
    private ProviderSession _providerSession;
    private DecodeIterator _dIter = CodecFactory.createDecodeIterator();
    private Msg _receivedMsg = CodecFactory.createMsg();
    private Error _error = TransportFactory.createError();
    private UnSupportedMsgHandler _unSupportedMsgHandler;
    private ProviderDictionaryHandler _dictionaryHandler;
    private ProviderDirectoryHandler _directoryHandler;
    private ProviderLoginHandler _loginHandler;
    private ItemHandler _itemHandler;

    private long _runtime;

    private static final int UPDATE_INTERVAL = 1;
    private long publishTime = 0;

    /* default server port number */
    private static final String defaultSrvrPortNo = "14002";

    /* default service name */
    private static final String defaultServiceName = "DIRECT_FEED";

    /* default run time in seconds */
    private static final String defaultRuntime = "1200"; // seconds
    
    public static final int CLIENT_SESSION_INIT_TIMEOUT = 30; // seconds
    
    public Provider()
    {
        _providerSession = new ProviderSession();
        _unSupportedMsgHandler = new UnSupportedMsgHandler(_providerSession);
        _dictionaryHandler = new ProviderDictionaryHandler(_providerSession);
        _directoryHandler = new ProviderDirectoryHandler(_providerSession);
        _loginHandler = new ProviderLoginHandler(_providerSession);
        _itemHandler = new ItemHandler(_providerSession, _dictionaryHandler, _loginHandler);
    }

    public static void main(String[] args)
    {
        Provider provider = new Provider();
        provider.init(args);
        provider.run();
        provider.uninit();
    }

    /**
     * Parses command line arguments, initializes provider session which creates
     * listening socket. It also initializes Login, Directory, Dictionary and
     * Item Handlers.
     * 
     * @param args - command line arguments
     */
    public void init(String[] args)
    {
        /* process command line args */
        addCommandLineArgs();
        try
        {
            CommandLine.parseArgs(args);
        }
        catch (IllegalArgumentException ile)
        {
            System.err.println("Error loading command line arguments:\t");
            System.err.println(ile.getMessage());
            System.err.println();
            System.err.println(CommandLine.optionHelpString());
            System.exit(CodecReturnCodes.FAILURE);
        }

        System.out.println("portNo: " + CommandLine.value("p"));
        System.out.println("interfaceName: " + CommandLine.value("i"));
        System.out.println("serviceName: " + CommandLine.value("s"));
        System.out.println("serviceId: " + CommandLine.value("id"));

        if ( ! _dictionaryHandler.loadDictionary(_error) )
        {
    	   /* if no local dictionary found maybe we can request it from ADH */
    	   System.out.println("Local dictionary not available, will try to request it from ADH if it supports the Provider Dictionary Download\n");
        }
        
        // get bind options from the provider session
        BindOptions bindOptions = _providerSession.getBindOptions();
        
        // set the connection parameters on the bind options
        bindOptions.serviceName(CommandLine.value("p"));
        bindOptions.interfaceName(CommandLine.value("i"));
        
        int ret = _providerSession.init(false, _error);
        if (ret != TransportReturnCodes.SUCCESS)
        {
            System.out.println("Error initializing server: " + _error.text());
            System.exit(TransportReturnCodes.FAILURE);
        }

        
        // enable XML tracing
        if (CommandLine.booleanValue("x"))
        {
            _providerSession.enableXmlTrace(_dictionaryHandler.dictionary());
        }

        _loginHandler.init();
        _directoryHandler.init();
        _directoryHandler.serviceName(CommandLine.value("s"));
        _itemHandler.init();
        try
        {
            _directoryHandler.serviceId(CommandLine.intValue("id"));
        	_itemHandler.serviceId(CommandLine.intValue("id"));
        	_runtime = System.currentTimeMillis() + CommandLine.intValue("runtime") * 1000;
        }
        catch (NumberFormatException ile)
        {
        	System.err.println("Invalid argument, number expected.\t");
        	System.err.println(ile.getMessage());
        	System.exit(-1);
        }
    }

    private static void addCommandLineArgs()
    {
        CommandLine.programName("UPA Provider");
        CommandLine.addOption("p", defaultSrvrPortNo, "Server port number");
        CommandLine.addOption("s", defaultServiceName, "Service name");
        CommandLine.addOption("i", (String)null, "Interface name");
        CommandLine.addOption("runtime", defaultRuntime, "Program runtime in seconds");
        CommandLine.addOption("id", "1", "Service id");
        CommandLine.addOption("x", "Provides XML tracing of messages.");
    }

    /*
     * Handles the run-time for the Provider. Sends close status messages to
     * all streams on all channels after run-time has elapsed.
     */
    private void handleRuntime()
    {
        // get current time
        long currentTime = System.currentTimeMillis();

        if (currentTime >= _runtime)
        {
            // send close status messages to all streams on all channels
            for (ClientSessionInfo clientSessionInfo : _providerSession.clientSessions)
            {                	
            	if ((clientSessionInfo != null) && 
            			(clientSessionInfo.clientChannel() != null && 
            			clientSessionInfo.clientChannel().selectableChannel() != null && 
            			clientSessionInfo.clientChannel().state() != ChannelState.INACTIVE))                	                		
                {
                    // send close status messages to all item streams 
                    int ret = _itemHandler.sendCloseStatusMsgs(clientSessionInfo.clientChannel(), _error);
                    if (ret != 0)
                        System.out.println("Error sending item close: " + _error.text());

                    // send close status message to source directory stream
                    ret = _directoryHandler.sendCloseStatus(clientSessionInfo.clientChannel(), _error);
                    if (ret != 0)
                        System.out.println("Error sending directory close: " + _error.text());

                    // send close status messages to dictionary streams
                    _dictionaryHandler.sendCloseStatusMsgs(clientSessionInfo.clientChannel(), _error);
                    if (ret != 0)
                        System.out.println("Error sending dictionary close: " + _error.text());
                                        
                    // send close status message to login stream 
                    ret = _loginHandler.sendCloseStatus(clientSessionInfo.clientChannel(), _error);
                    if (ret != 0)
                        System.out.println("Error sending login close: " + _error.text());

                    // flush before exiting 
                    if ( clientSessionInfo.clientChannel() != null && clientSessionInfo.clientChannel().selectableChannel() != null) 
                    {
                    	SelectionKey key = clientSessionInfo.clientChannel().selectableChannel().keyFor(_providerSession.selector);
                    
                    	if (key != null && key.isValid() && key.isWritable())
                    	{
                    		ret = 1;
                    		while (ret > TransportReturnCodes.SUCCESS)
                    		{
                    			ret = clientSessionInfo.clientChannel().flush(_error);
                    		}
                    		if (ret < TransportReturnCodes.SUCCESS)
                    		{
                    			System.out.println("clientChannel.flush() failed with return code " + ret + _error.text());
                    		}
                    	}
                    }
                }
            }
            System.out.println("provider run-time expired...");
            uninit();
            System.exit(0);
        }

    }

    /**
     * Main loop polls socket events from server socket. Accepts new client
     * connections and reads requests from already established client
     * connection. Checks for runtime expiration. If there is no activity on the
     * socket, periodically sends item updates to connected client sessions that
     * has requested market price items.
     */
    public void run()
    {
        int ret = 0;
 
        // main loop
        while (true)
        {
            Set<SelectionKey> keySet = null;
            try
            {
                if (_providerSession.selector.select(UPDATE_INTERVAL * 200) > 0)  
                {
                    keySet = _providerSession.selector.selectedKeys();
                }
            }
            catch (IOException e1)
            {
                System.out.println(e1.getMessage());
                cleanupAndExit();
            }

            if (publishTime < System.currentTimeMillis())
            {
                /* Send market price updates for each connected channel */
                _itemHandler.updateItemInfo();
                
                for (ClientSessionInfo clientSessionInfo : _providerSession.clientSessions)
                {   
                    if ((clientSessionInfo != null) && 
                    		(clientSessionInfo.clientChannel() != null && 
                    				clientSessionInfo.clientChannel().selectableChannel() != null && 
                    				clientSessionInfo.clientChannel().state() != ChannelState.INACTIVE))
                    {                        	
                    	ret = _itemHandler.sendItemUpdates(clientSessionInfo.clientChannel(), _error);
                        if (ret != CodecReturnCodes.SUCCESS)
                        {
                        	System.out.println(_error.text());                            
                            processChannelClose(clientSessionInfo.clientChannel());
                            _providerSession.removeClientSessionForChannel(clientSessionInfo.clientChannel(), _error);                            
                            removeInactiveSessions();
                        }
                    }
                }
                publishTime = System.currentTimeMillis() + 1000;
            }
            
            if (keySet != null)
            {
            	checkTimeout();           	
                Iterator<SelectionKey> iter = keySet.iterator();
                                
                while (iter.hasNext())
                {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if(!key.isValid())
                    {
                    	key.cancel();
                        continue;
                    }
                    if (key.isAcceptable())
                    {
                        ret = _providerSession.handleNewClientSession((Server)key.attachment(), _error);
                        if (ret != TransportReturnCodes.SUCCESS)
                        {
                        	System.out.println("accept error, text: " + _error.text());
                            continue;
                        }
                    }
                    else if (key.isReadable())
                    {               
                        ret = _providerSession.read((Channel)key.attachment(), _error, this);
                        if (ret != TransportReturnCodes.SUCCESS)
                        {                        	
                           	try
                        	{
                        		key.channel().close();
                        	}
                        	catch(Exception e)
                        	{
                        	}                        	
                            System.out.println("read error, text: " + _error.text());                  
                            continue;
                        }
                    }
                    else if (key.isWritable() && ((Channel)key.attachment()).state() == ChannelState.ACTIVE)
                    {
                        _providerSession.flush(key, _error);     
                    }
                }
            }
 
            /* Handle pings */           
            _providerSession.handlePings();

            /* Handle run-time */
            handleRuntime();
        }
    }

    /**
     * Call back for socket read for client messages.
     */
    public void processReceivedMsg(Channel channel, TransportBuffer msgBuf)
    {
        /* clear decode iterator */
        _dIter.clear();

        /* set buffer and version info */
        int ret = _dIter.setBufferAndRWFVersion(msgBuf, channel.majorVersion(), channel.minorVersion());
        if (ret != CodecReturnCodes.SUCCESS)
        {
            System.out.println("DecodeIterator.setBufferAndRWFVersion() failed with return code: " + CodecReturnCodes.toString(ret));
            processChannelClose(channel);
            _providerSession.removeClientSessionForChannel(channel, _error);
            removeInactiveSessions();

        }
        
        ret = _receivedMsg.decode(_dIter);
        if (ret != CodecReturnCodes.SUCCESS)
        {
            System.out.println("RequestMsg.decode() failed with return code: " + CodecReturnCodes.toString(ret) + " on SessionData " + channel.selectableChannel() + "  Size " + (msgBuf.data().limit() - msgBuf.data().position()));
            processChannelClose(channel);
            _providerSession.removeClientSessionForChannel(channel, _error);
            removeInactiveSessions();
        }

        switch (_receivedMsg.domainType())
        {
            case DomainTypes.LOGIN:
            {
                if (_loginHandler.processRequest(channel, _receivedMsg, _dIter, _error) != 0)
                {
                    System.out.println("Error processing login request: " + _error.text());
                    processChannelClose(channel);
                    _providerSession.removeClientSessionForChannel(channel, _error);
                    removeInactiveSessions();
                }
                
                // request dictionary from ADH if not available locally
                if ( ! _dictionaryHandler.isDictionaryReady() )
                {
                	LoginRequestInfo loginReqInfo = _loginHandler.findLoginRequestInfo(channel);

                	if( loginReqInfo.loginRequest().checkHasAttrib() &&
                		loginReqInfo.loginRequest().attrib().checkHasProviderSupportDictionaryDownload() && 
                		loginReqInfo.loginRequest().attrib().supportProviderDictionaryDownload() ==1 )
                	{
                		int requestStatus = _dictionaryHandler.sendDictionaryRequests(channel,_error,_directoryHandler.serviceId());
                		if( requestStatus == CodecReturnCodes.SUCCESS )
                		{
                			System.out.println("Sent Dictionary Request\n");
                		}
                		else
                		{
                			System.out.println("Dictionary could not be downloaded, unable to send the request to the connection "+_error.text());
                            processChannelClose(channel);
                            _providerSession.removeClientSessionForChannel(channel, _error);
                            removeInactiveSessions();
                		}
                	}
                	else
                	{
                		System.out.println("Dictionary could not be downloaded, the connection does not support Provider Dictionary Download");
                        processChannelClose(channel);
                        _providerSession.removeClientSessionForChannel(channel, _error);
                        removeInactiveSessions();
                	}
                }
                break;
            }

            case DomainTypes.SOURCE:
                if (_directoryHandler.processRequest(channel, _receivedMsg, _dIter, _error) != 0)
                {
                    System.out.println("Error processing directory request: " + _error.text());
                    processChannelClose(channel);
                    _providerSession.removeClientSessionForChannel(channel, _error);
                    removeInactiveSessions();
                }
                break;
            case DomainTypes.DICTIONARY:
            	if(_dictionaryHandler.processMessage(channel,_receivedMsg, _dIter, _error) != 0)
                {
                    System.out.println("Error processing dictionary message: " + _error.text());
                    processChannelClose(channel);
                    _providerSession.removeClientSessionForChannel(channel, _error);
                    removeInactiveSessions();
                }
                break;
                
            case DomainTypes.MARKET_PRICE:
            case DomainTypes.MARKET_BY_ORDER:
            case DomainTypes.MARKET_BY_PRICE:
            case DomainTypes.SYMBOL_LIST:
                if (_itemHandler.processRequest(channel, _receivedMsg, _dIter, _error) != 0)
                {
                    System.out.println("Error processing item request: " + _error.text());
                    processChannelClose(channel);
                    _providerSession.removeClientSessionForChannel(channel, _error);
                    removeInactiveSessions();
                }
                break;
            default:
                if (_unSupportedMsgHandler.processRequest(channel, _receivedMsg, _error) != 0)
                {
                    System.out.println("Error processing unhandled request message: " + _error.text());
                    processChannelClose(channel);
                    _providerSession.removeClientSessionForChannel(channel, _error);
                    removeInactiveSessions();
                }
                break;
        }
    }

    private void checkTimeout()
    {
       	for (ClientSessionInfo clientSessionInfo : _providerSession.clientSessions)
        {  		
        	if (clientSessionInfo != null && 
        			clientSessionInfo.clientChannel() != null && 
        			clientSessionInfo.clientChannel().state() == ChannelState.INITIALIZING)
        	{
        		if ((System.currentTimeMillis() - clientSessionInfo.startTime()) > CLIENT_SESSION_INIT_TIMEOUT * 1000)
        		{	
        			System.out.println("Provider close clientSesson due to timeout of initialization " + clientSessionInfo.clientChannel().selectableChannel() +  " curTime = " + System.currentTimeMillis() +  " startTime = " +  clientSessionInfo.startTime());               		
        			_providerSession.removeClientSessionForChannel(clientSessionInfo.clientChannel(), _error);
        			removeInactiveSessions();
        		}
         	}
        }
    }
    
    private void removeInactiveSessions()
    {
       	for (ClientSessionInfo clientSessionInfo : _providerSession.clientSessions)
        {
            if (clientSessionInfo != null && 
            		clientSessionInfo.clientChannel() != null && 
            		 clientSessionInfo.clientChannel().state() == ChannelState.INACTIVE && clientSessionInfo.startTime() > 0 )
            {
        		System.out.println("Provider close clientSesson due to inactive state ");
        		_providerSession.removeInactiveClientSessionForChannel(clientSessionInfo, _error);            	
            }                        
        }
    }
  
    
    private void uninit()
    {
    	_providerSession.uninit();
    }

    private void cleanupAndExit()
    {
        _providerSession.uninit();
        System.exit(TransportReturnCodes.FAILURE);
    }

	@Override
	public void processChannelClose(Channel channel)
	{
		_itemHandler.closeRequests(channel);
		_dictionaryHandler.closeRequests(channel);
		_directoryHandler.closeRequest(channel);
		_loginHandler.closeRequest(channel);
	}
}
