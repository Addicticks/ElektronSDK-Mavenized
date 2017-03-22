package com.thomsonreuters.upa.perftools.common;

import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.codec.EncodeIterator;
import com.thomsonreuters.upa.codec.FilterEntryActions;
import com.thomsonreuters.upa.codec.MapEntryActions;
import com.thomsonreuters.upa.codec.Qos;
import com.thomsonreuters.upa.codec.QosRates;
import com.thomsonreuters.upa.codec.QosTimeliness;
import com.thomsonreuters.upa.rdm.DomainTypes;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryMsgFactory;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryMsgType;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryRefresh;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.Service;

/**
 * The directory handler for the upajProvPerf and upajNIProvPerf. Configures a
 * single service and provides encoding and sending of a directory message.
 */
public class DirectoryProvider
{
    protected static final int            REFRESH_MSG_SIZE = 1024;

    // vendor name
    protected static final String         vendor = "Thomson Reuters";

    // field dictionary used and provided for the source. 
    protected static final String         fieldDictionaryName = "RWFFld";

    // enum dictionary used and provided for the source.
    protected static final String         enumTypeDictionaryName = "RWFEnum";

    protected DirectoryRefresh            _directoryRefresh;
    
    protected EncodeIterator              _encodeIter;

    // Source service information 
    protected Service                     _service;
    
    // Open limit
    protected int                         _openLimit;
    
    // Service id 
    protected int                         _serviceId;
    
    // Service name
    protected String                      _serviceName;
    
    // Service qos
    protected Qos                         _qos;

    public DirectoryProvider()
    {
        _directoryRefresh = (DirectoryRefresh)DirectoryMsgFactory.createMsg();
        _directoryRefresh.rdmMsgType(DirectoryMsgType.REFRESH);
        _service = DirectoryMsgFactory.createService();
        _qos = CodecFactory.createQos();
        _directoryRefresh.rdmMsgType(DirectoryMsgType.REFRESH);
        
        _qos.dynamic(false);
        _qos.rate(QosRates.TICK_BY_TICK);
        _qos.timeliness(QosTimeliness.REALTIME);
        
        _encodeIter = CodecFactory.createEncodeIterator();
    }

    /**
     * Initializes service information to be published with directory messages.
     *  
     * @param xmlMsgData XML messages that provider sends
     */
    public void initService(XmlMsgData xmlMsgData)
    {
        _service.clear();

        _service.flags(Service.ServiceFlags.HAS_INFO | Service.ServiceFlags.HAS_STATE | Service.ServiceFlags.HAS_LOAD);
        _service.serviceId(_serviceId);
        _service.action(MapEntryActions.ADD);

        // Info 
        _service.info().action(FilterEntryActions.SET);
        _service.info().serviceName().data(_serviceName);

        _service.info().applyHasVendor();
        _service.info().vendor().data(vendor);

        _service.info().applyHasIsSource();
        _service.info().isSource(1);

        _service.info().capabilitiesList().add((long)DomainTypes.DICTIONARY);
        if (xmlMsgData.hasMarketPrice())
        {
            _service.info().capabilitiesList().add((long)DomainTypes.MARKET_PRICE);
        }

        _service.info().applyHasDictionariesProvided();
        _service.info().dictionariesProvidedList().add(enumTypeDictionaryName);
        _service.info().dictionariesProvidedList().add(fieldDictionaryName);

        _service.info().applyHasDictionariesUsed();
        _service.info().dictionariesUsedList().add(enumTypeDictionaryName);
        _service.info().dictionariesUsedList().add(fieldDictionaryName);

        _service.info().applyHasQos();
        _service.info().qosList().add(_qos);

        _service.info().applyHasSupportsQosRange();
        _service.info().supportsQosRange(0);

        _service.info().applyHasSupportsOutOfBandSnapshots();
        _service.info().supportsOutOfBandSnapshots(0);

        // State
        _service.state().action(FilterEntryActions.SET);
        _service.state().serviceState(1);
        _service.state().applyHasAcceptingRequests();
        _service.state().acceptingRequests(1);

        // Load 
        if (_openLimit > 0)
        {
            _service.load().action(FilterEntryActions.SET);
            _service.load().applyHasOpenLimit();
            _service.load().openLimit(_openLimit);
        }
    }

	/**
	 * @return Service id configured for the provider
	 */
    public int serviceId()
    {
        return _serviceId;
    }

    /**
     * Configure service id for the provider.
     * @param serviceId
     */
    public void serviceId(int serviceId)
    {
        this._serviceId = serviceId;
    }

    /**
     * @return configured qos information for the provider.
     */
    public Qos qos()
    {
        return _qos;
    }

    /**
     * @return Service name configured for the provider
     */
    public String serviceName()
    {
        return _serviceName;
    }

    /**
     * Configure service name for the provider.
     * @param serviceName
     */
    public void serviceName(String serviceName)
    {
        _serviceName = serviceName;
    }

    /**
     * @return open limit configured for the provider
     */
    public int openLimit()
    {
        return _openLimit;
    }

    /**
     * Configure open limit for the provider.
     * @param openLimit
     */
    public void openLimit(int openLimit)
    {
        _openLimit = openLimit;
    }
    
    /**
     * @return directory refresh
     */
    public DirectoryRefresh directoryRefresh()
    {
        return _directoryRefresh;
    }
}