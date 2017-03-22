package com.thomsonreuters.ema.access;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.thomsonreuters.ema.access.OmmLoggerClient.Severity;
import com.thomsonreuters.upa.transport.ComponentInfo;
import com.thomsonreuters.upa.valueadd.reactor.ReactorCallbackReturnCodes;
import com.thomsonreuters.upa.valueadd.reactor.ReactorChannel;
import com.thomsonreuters.upa.valueadd.reactor.ReactorChannelEvent;
import com.thomsonreuters.upa.valueadd.reactor.ReactorChannelEventCallback;
import com.thomsonreuters.upa.valueadd.reactor.ReactorChannelEventTypes;
import com.thomsonreuters.upa.valueadd.reactor.ReactorChannelInfo;
import com.thomsonreuters.upa.valueadd.reactor.ReactorErrorInfo;
import com.thomsonreuters.upa.valueadd.reactor.ReactorFactory;
import com.thomsonreuters.upa.valueadd.reactor.ReactorReturnCodes;


class ServerChannelHandler implements ReactorChannelEventCallback
{
    HashMap<LongObject, ClientSession> _clientSessionMap = new HashMap<LongObject, ClientSession>();
    OmmServerBaseImpl _serverImpl;
    ReactorErrorInfo _errorInfo = ReactorFactory.createReactorErrorInfo();

    private static final String CLIENT_NAME = "ServerChannelHandler";

    ServerChannelHandler(OmmServerBaseImpl serverBaseImpl)
    {
        _serverImpl = serverBaseImpl;
    }

    public int reactorChannelEventCallback(ReactorChannelEvent event)
    {
        ClientSession clientSession = (ClientSession)event.reactorChannel().userSpecObj();
        ReactorChannel rsslReactorChannel = event.reactorChannel();

        switch (event.eventType())
        {
            case ReactorChannelEventTypes.CHANNEL_OPENED:
            {
                if (_serverImpl.loggerClient().isTraceEnabled())
                {
                    StringBuilder temp = _serverImpl.strBuilder();
                    temp.append("Received ChannelOpened on client handle ");
                    temp.append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR)
                    .append("Instance Name ").append(_serverImpl.activeConfig().instanceName);
                    _serverImpl.loggerClient().trace(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.TRACE));
                }
                
                return ReactorCallbackReturnCodes.SUCCESS;
            }
            case ReactorChannelEventTypes.CHANNEL_UP:
            {
                ReactorChannelInfo reactorChannelInfo = new ReactorChannelInfo();
                ReactorErrorInfo errorInfo = event.errorInfo();

                try
                {
                    event.reactorChannel().selectableChannel().register(_serverImpl.selector(), SelectionKey.OP_READ, event.reactorChannel());
                }
                catch (ClosedChannelException e)
                {
                    if (_serverImpl.loggerClient().isErrorEnabled())
                    {
                        StringBuilder temp = _serverImpl.strBuilder();
                        temp.append("Selector failed to register client handle ").append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR)
                        .append("Instance Name ").append(_serverImpl.activeConfig().instanceName).append(OmmLoggerClient.CR);
                        if (rsslReactorChannel != null && rsslReactorChannel.channel() != null)
                        {
                            temp.append("RsslReactor ").append("@").append(Integer.toHexString(rsslReactorChannel.reactor().hashCode())).append(OmmLoggerClient.CR).append("RsslChannel ").append("@")
                            .append(Integer.toHexString(rsslReactorChannel.channel().hashCode())).append(OmmLoggerClient.CR);
                        }
                        else
                        {
                            temp.append("RsslReactor Channel is null").append(OmmLoggerClient.CR);
                        }

                        _serverImpl.loggerClient().error(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.ERROR));
                    }
                    
                    return ReactorCallbackReturnCodes.SUCCESS;
                }

                addClientSession(clientSession);
                clientSession.channel(event.reactorChannel());
                clientSession.channel().info(reactorChannelInfo, errorInfo);
                
                ServerConfig serverConfig = _serverImpl.activeConfig().serverConfig;

                if (rsslReactorChannel.ioctl(com.thomsonreuters.upa.transport.IoctlCodes.SYSTEM_WRITE_BUFFERS, serverConfig.sysSendBufSize, errorInfo) != ReactorReturnCodes.SUCCESS)
                {
                    if (_serverImpl.loggerClient().isErrorEnabled())
                    {
                        StringBuilder temp = _serverImpl.strBuilder();
                        temp.append("Failed to set send buffer size on client handle ").append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR)
                        .append("Instance Name ").append(_serverImpl.activeConfig().instanceName).append(OmmLoggerClient.CR);
                        
                        if (rsslReactorChannel != null && rsslReactorChannel.channel() != null)
                        {
                            temp.append("RsslReactor ").append("@").append(Integer.toHexString(rsslReactorChannel.reactor().hashCode()))
                            .append(OmmLoggerClient.CR).append("RsslChannel ").append("@").append(Integer.toHexString(rsslReactorChannel.channel().hashCode())).append(OmmLoggerClient.CR);
                        }
                        else
                        {
                            temp.append("RsslReactor Channel is null").append(OmmLoggerClient.CR);
                        }

                        temp.append("Error Id ").append(errorInfo.error().errorId()).append(OmmLoggerClient.CR)
                        .append("Internal sysError ").append(errorInfo.error().sysError()).append(OmmLoggerClient.CR).append("Error Location ")
                        .append(errorInfo.location()).append(OmmLoggerClient.CR).append("Error text ").append(errorInfo.error().text());

                        _serverImpl.loggerClient().error(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.ERROR));
                    }
                    
                    closeChannel(rsslReactorChannel);

                    return ReactorCallbackReturnCodes.SUCCESS;
                }

                if (rsslReactorChannel.ioctl(com.thomsonreuters.upa.transport.IoctlCodes.SYSTEM_READ_BUFFERS, serverConfig.sysRecvBufSize, errorInfo) != ReactorReturnCodes.SUCCESS)
                {
                    if (_serverImpl.loggerClient().isErrorEnabled())
                    {
                        StringBuilder temp = _serverImpl.strBuilder();
                        temp.append("Failed to set receive buffer size on client handle ").append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR)
                        .append("Instance Name ").append(_serverImpl.activeConfig().instanceName).append(OmmLoggerClient.CR);
                        
                        if (rsslReactorChannel != null && rsslReactorChannel.channel() != null)
                        {
                            temp.append("RsslReactor ").append("@").append(Integer.toHexString(rsslReactorChannel.reactor().hashCode()))
                            .append(OmmLoggerClient.CR).append("RsslChannel ").append("@").append(Integer.toHexString(rsslReactorChannel.channel().hashCode())).append(OmmLoggerClient.CR);
                        }
                        else
                        {
                            temp.append("RsslReactor Channel is null").append(OmmLoggerClient.CR);
                        }

                        temp.append("Error Id ").append(errorInfo.error().errorId()).append(OmmLoggerClient.CR).append("Internal sysError ")
                        .append(errorInfo.error().sysError()).append(OmmLoggerClient.CR).append("Error Location ").append(errorInfo.location()).append(OmmLoggerClient.CR)
                        .append("Error text ").append(errorInfo.error().text());

                        _serverImpl.loggerClient().error(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.ERROR));
                    }

                    closeChannel(rsslReactorChannel);

                    return ReactorCallbackReturnCodes.SUCCESS;
                }

                if (rsslReactorChannel.ioctl(com.thomsonreuters.upa.transport.IoctlCodes.COMPRESSION_THRESHOLD, serverConfig.compressionThreshold, errorInfo) != ReactorReturnCodes.SUCCESS)
                {
                    if (_serverImpl.loggerClient().isErrorEnabled())
                    {
                        StringBuilder temp = _serverImpl.strBuilder();

                        temp.append("Failed to set compression threshold on channel ").append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR)
                        .append("Instance Name ").append(_serverImpl.activeConfig().instanceName).append(OmmLoggerClient.CR);
                        
                        if (rsslReactorChannel != null && rsslReactorChannel.channel() != null)
                        {
                            temp.append("RsslReactor ").append("@").append(Integer.toHexString(rsslReactorChannel.reactor().hashCode()))
                            .append(OmmLoggerClient.CR).append("RsslChannel ").append("@").append(Integer.toHexString(rsslReactorChannel.channel().hashCode())).append(OmmLoggerClient.CR);
                        }
                        else
                        {
                            temp.append("RsslReactor Channel is null").append(OmmLoggerClient.CR);
                        }

                        temp.append("Error Id ").append(errorInfo.error().errorId()).append(OmmLoggerClient.CR).append("Internal sysError ").append(errorInfo.error().sysError())
                        .append(OmmLoggerClient.CR).append("Error Location ").append(errorInfo.location()).append(OmmLoggerClient.CR).append("Error text ").append(errorInfo.error().text());

                        _serverImpl.loggerClient().error(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.ERROR));
                    }

                    closeChannel(rsslReactorChannel);

                    return ReactorCallbackReturnCodes.SUCCESS;
                }

                if (_serverImpl.loggerClient().isInfoEnabled())
                {
                	List<ComponentInfo> componentInfoList = reactorChannelInfo.channelInfo().componentInfo();
                	
                    StringBuilder temp = _serverImpl.strBuilder();
                    temp.append("Received ChannelUp event on ClientHandle ");
                    temp.append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR)
                        .append("Instance Name ").append(_serverImpl.activeConfig().instanceName).append(OmmLoggerClient.CR);
                        
                        if (componentInfoList.size() != 0 )
                        {
                        	temp.append("Component Version ").append(componentInfoList.get(0).componentVersion().toString());
                        }
                        else
                        {
                        	temp.append("Component Version ").append("");
                        }
                        
                    _serverImpl.loggerClient().info(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.INFO));
                }

                if (serverConfig.highWaterMark != 0)
                {
                    if (rsslReactorChannel.ioctl(com.thomsonreuters.upa.transport.IoctlCodes.HIGH_WATER_MARK, serverConfig.highWaterMark, errorInfo) != ReactorReturnCodes.SUCCESS)
                    {
                        if (_serverImpl.loggerClient().isErrorEnabled())
                        {
                            StringBuilder temp = _serverImpl.strBuilder();

                            temp.append("Failed to set high water mark on client handle ").append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR)
                            .append("Instance Name ").append(_serverImpl.activeConfig().instanceName).append(OmmLoggerClient.CR);
                            
                            if (rsslReactorChannel != null && rsslReactorChannel.channel() != null)
                            {
                                temp.append("RsslReactor ").append("@").append(Integer.toHexString(rsslReactorChannel.reactor().hashCode()))
                                .append(OmmLoggerClient.CR).append("RsslChannel ").append("@").append(Integer.toHexString(rsslReactorChannel.channel().hashCode())).append(OmmLoggerClient.CR);
                            }
                            else
                            {
                                temp.append("RsslReactor Channel is null").append(OmmLoggerClient.CR);
                            }

                            temp.append("Error Id ").append(errorInfo.error().errorId()).append(OmmLoggerClient.CR).append("Internal sysError ")
                            .append(errorInfo.error().sysError()).append(OmmLoggerClient.CR).append("Error Location ").append(errorInfo.location()).append(OmmLoggerClient.CR)
                            .append("Error text ").append(errorInfo.error().text());

                            _serverImpl.loggerClient().error(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.ERROR));
                        }

                        closeChannel(rsslReactorChannel);

                        return ReactorCallbackReturnCodes.SUCCESS;
                    }
                    else if (_serverImpl.loggerClient().isTraceEnabled())
                    {
                        StringBuilder temp = _serverImpl.strBuilder();
                        temp.append("High water mark set on client handle ");
                        temp.append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR).append("Instance Name ").append(_serverImpl.activeConfig().instanceName);
                        _serverImpl.loggerClient().info(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.TRACE));
                    }
                }

                return ReactorCallbackReturnCodes.SUCCESS;
            }
            case ReactorChannelEventTypes.FD_CHANGE:
            {
                try
                {
                    SelectionKey key = event.reactorChannel().oldSelectableChannel().keyFor(_serverImpl.selector());
                    if (key != null)
                        key.cancel();
                }
                catch (Exception e)
                {
                }

                try
                {
                    event.reactorChannel().selectableChannel().register(_serverImpl.selector(), SelectionKey.OP_READ, event.reactorChannel());
                }
                catch (Exception e)
                {
                    if (_serverImpl.loggerClient().isErrorEnabled())
                    {
                        StringBuilder temp = _serverImpl.strBuilder();
                        temp.append("Selector failed to register client handle ").append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR);
                        if (rsslReactorChannel != null && rsslReactorChannel.channel() != null)
                        {
                            temp.append("RsslReactor ").append("@").append(Integer.toHexString(rsslReactorChannel.reactor().hashCode()))
                            .append(OmmLoggerClient.CR).append("RsslChannel ").append("@").append(Integer.toHexString(rsslReactorChannel.channel().hashCode())).append(OmmLoggerClient.CR);
                        }
                        else
                        {
                            temp.append("RsslReactor Channel is null").append(OmmLoggerClient.CR);
                        }

                        _serverImpl.loggerClient().error(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.ERROR));
                    }
                    
                    closeChannel(rsslReactorChannel);
                    
                    return ReactorCallbackReturnCodes.SUCCESS;
                }

                if (_serverImpl.loggerClient().isTraceEnabled())
                {
                    StringBuilder temp = _serverImpl.strBuilder();
                    temp.append("Received FD Change event on client handle ").append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR).append("Instance Name ")
                    .append(_serverImpl.activeConfig().instanceName);
                    
                    _serverImpl.loggerClient().trace(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.TRACE));
                }

                clientSession.channel(event.reactorChannel());

                return ReactorCallbackReturnCodes.SUCCESS;
            }
            case ReactorChannelEventTypes.CHANNEL_READY:
            {
                if (_serverImpl.loggerClient().isTraceEnabled())
                {
                    StringBuilder temp = _serverImpl.strBuilder();
                    temp.append("Received ChannelReady event on client handle ");
                    temp.append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR).append("Instance Name ").append(_serverImpl.activeConfig().instanceName);
                    _serverImpl.loggerClient().trace(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.TRACE));
                }

                return ReactorCallbackReturnCodes.SUCCESS;
            }
            case ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING:
            {
                try
                {
                    SelectionKey key = event.reactorChannel().selectableChannel().keyFor(_serverImpl.selector());
                    if (key != null)
                        key.cancel();
                }
                catch (Exception e)
                {
                }

                if (_serverImpl.loggerClient().isWarnEnabled())
                {
                    ReactorErrorInfo errorInfo = event.errorInfo();

                    StringBuilder temp = _serverImpl.strBuilder();
                    temp.append("Received ChannelDownReconnecting event on client handle ").append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR);
                    
                    if (rsslReactorChannel != null && rsslReactorChannel.channel() != null)
                    {
                        temp.append("RsslReactor ").append("@").append(Integer.toHexString(rsslReactorChannel.reactor().hashCode())).append(OmmLoggerClient.CR)
                        .append("RsslChannel ").append("@").append(Integer.toHexString(rsslReactorChannel.channel().hashCode())).append(OmmLoggerClient.CR);
                    }
                    else
                    {
                        temp.append("RsslReactor Channel is null").append(OmmLoggerClient.CR);
                    }

                    temp.append("Error Id ").append(event.errorInfo().error().errorId()).append(OmmLoggerClient.CR).append("Internal sysError ")
                    .append(errorInfo.error().sysError()).append(OmmLoggerClient.CR).append("Error Location ").append(errorInfo.location()).append(OmmLoggerClient.CR)
                    .append("Error text ").append(errorInfo.error().text());

                    _serverImpl.loggerClient().warn(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.WARNING));
                }

                return ReactorCallbackReturnCodes.SUCCESS;
            }
            case ReactorChannelEventTypes.CHANNEL_DOWN:
            {
                ReactorErrorInfo errorInfo = event.errorInfo();

                try
                {
                    SelectionKey key = rsslReactorChannel.selectableChannel().keyFor(_serverImpl.selector());
                    if (key != null)
                        key.cancel();
                }
                catch (Exception e)
                {
                }

                if (_serverImpl.loggerClient().isWarnEnabled())
                {
                    StringBuilder temp = _serverImpl.strBuilder();
                    temp.append("Received ChannelDown event on client handle ").append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR).append("Instance Name ")
                    .append(_serverImpl.activeConfig().instanceName).append(OmmLoggerClient.CR);
                    
                    if (rsslReactorChannel != null && rsslReactorChannel.channel() != null)
                    {
                        temp.append("RsslReactor ").append("@").append(Integer.toHexString(rsslReactorChannel.reactor().hashCode()))
                        .append(OmmLoggerClient.CR).append("RsslChannel ").append("@").append(Integer.toHexString(rsslReactorChannel.channel().hashCode())).append(OmmLoggerClient.CR);
                    }
                    else
                    {
                        temp.append("RsslReactor Channel is null").append(OmmLoggerClient.CR);
                    }

                    temp.append("Error Id ").append(event.errorInfo().error().errorId()).append(OmmLoggerClient.CR).append("Internal sysError ")
                    .append(errorInfo.error().sysError()).append(OmmLoggerClient.CR).append("Error Location ").append(errorInfo.location()).append(OmmLoggerClient.CR)
                    .append("Error text ").append(errorInfo.error().text());

                    _serverImpl.loggerClient().error(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.WARNING));
                }
                
                if ( _serverImpl.state() != OmmServerBaseImpl.OmmImplState.UNINITIALIZING)
                {
                	_serverImpl.loginHandler().notifyChannelDown(clientSession);
                }

                closeChannel(rsslReactorChannel);

                return ReactorCallbackReturnCodes.SUCCESS;
            }
            case ReactorChannelEventTypes.WARNING:
            {
                if (_serverImpl.loggerClient().isWarnEnabled())
                {
                    ReactorErrorInfo errorInfo = event.errorInfo();

                    StringBuilder temp = _serverImpl.strBuilder();
                    temp.append("Received Channel warning event on client handle ").append(clientSession.clientHandle().value()).append(OmmLoggerClient.CR).append("Instance Name ")
                    .append(_serverImpl.activeConfig().instanceName).append(OmmLoggerClient.CR);
                    
                    if (rsslReactorChannel != null && rsslReactorChannel.channel() != null)
                    {
                        temp.append("RsslReactor ").append("@").append(Integer.toHexString(rsslReactorChannel.reactor().hashCode())).append(OmmLoggerClient.CR)
                        .append("RsslChannel ").append("@").append(Integer.toHexString(rsslReactorChannel.channel().hashCode())).append(OmmLoggerClient.CR);
                    }
                    else
                    {
                        temp.append("RsslReactor Channel is null").append(OmmLoggerClient.CR);
                    }

                    temp.append("Error Id ").append(event.errorInfo().error().errorId()).append(OmmLoggerClient.CR).append("Internal sysError ")
                    .append(errorInfo.error().sysError()).append(OmmLoggerClient.CR).append("Error Location ").append(errorInfo.location()).append(OmmLoggerClient.CR)
                    .append("Error text ").append(errorInfo.error().text());

                    _serverImpl.loggerClient().warn(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.WARNING));
                }
                
                return ReactorCallbackReturnCodes.SUCCESS;
            }
            default:
            {
                if (_serverImpl.loggerClient().isErrorEnabled())
                {
                    ReactorErrorInfo errorInfo = event.errorInfo();
                    StringBuilder temp = _serverImpl.strBuilder();
                    temp.append("Received unknown channel event type ").append(event.eventType()).append(OmmLoggerClient.CR).append("Instance Name ")
                    .append(_serverImpl.activeConfig().instanceName).append(OmmLoggerClient.CR);
                    
                    if (rsslReactorChannel != null && rsslReactorChannel.channel() != null)
                    {
                        temp.append("RsslReactor ").append("@").append(Integer.toHexString(rsslReactorChannel.reactor().hashCode())).append(OmmLoggerClient.CR).append("RsslChannel ")
                        .append("@").append(Integer.toHexString(rsslReactorChannel.channel().hashCode())).append(OmmLoggerClient.CR);
                    }
                    else
                    {
                        temp.append("RsslReactor Channel is null").append(OmmLoggerClient.CR);
                    }

                    temp.append("Error Id ").append(event.errorInfo().error().errorId()).append(OmmLoggerClient.CR).append("Internal sysError ")
                    .append(errorInfo.error().sysError()).append(OmmLoggerClient.CR).append("Error Location ").append(errorInfo.location()).append(OmmLoggerClient.CR)
                    .append("Error text ").append(errorInfo.error().text());

                    _serverImpl.loggerClient().error(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.ERROR));
                }
                
                return ReactorCallbackReturnCodes.SUCCESS;
            }
        }
    }

    ClientSession getClientSession(LongObject clientHandle)
    {
        return _clientSessionMap.get(clientHandle);
    }

    void addClientSession(ClientSession clientSession)
    {
        _clientSessionMap.put(clientSession.clientHandle(), clientSession);
    }

    void removeClientSession(ClientSession clientSession)
    {
        if (_clientSessionMap.remove(clientSession.clientHandle()) != null )
        {
        	clientSession.returnToPool();
        }
    }

    void closeChannel(ReactorChannel channel)
    {
        assert (channel != null);
        _errorInfo.clear();

        if (channel.close(_errorInfo) != ReactorReturnCodes.SUCCESS)
        {
            if (_serverImpl.loggerClient().isErrorEnabled())
            {
                StringBuilder temp = _serverImpl.strBuilder();
                temp.append("Failed to close reactor channel ").append(channel).append(OmmLoggerClient.CR);
                temp.append("Error Id ").append(_errorInfo.error().errorId()).append(OmmLoggerClient.CR).append("Internal sysError ")
                .append(_errorInfo.error().sysError()).append(OmmLoggerClient.CR).append("Error Location ").append(_errorInfo.location())
                .append(OmmLoggerClient.CR).append("Error text ").append(_errorInfo.error().text());

                _serverImpl.loggerClient().error(_serverImpl.formatLogMessage(ServerChannelHandler.CLIENT_NAME, temp.toString(), Severity.ERROR));
            }
        }

        removeChannel(channel);
    }

    void removeChannel(ReactorChannel channel)
    {
        ClientSession clientSession = (ClientSession)channel.userSpecObj();

        clientSession.closeAllItemInfo();
        removeClientSession(clientSession);
    }

    public void initialize()
    {

    }

    public void closeActiveSessions()
    {
        Iterator<Entry<LongObject, ClientSession>> iter = _clientSessionMap.entrySet().iterator();
        while (iter.hasNext())
        {
            ClientSession clientSession = (ClientSession)iter.next().getValue();
            clientSession.closeAllItemInfo();
            clientSession.returnToPool();
        }
        
        _clientSessionMap.clear();
    }
}
