package com.thomsonreuters.upa.examples.niprovider;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.codec.CodecReturnCodes;
import com.thomsonreuters.upa.codec.DataDictionary;
import com.thomsonreuters.upa.codec.DataStates;
import com.thomsonreuters.upa.codec.EncodeIterator;
import com.thomsonreuters.upa.codec.FieldEntry;
import com.thomsonreuters.upa.codec.FieldList;
import com.thomsonreuters.upa.codec.QosRates;
import com.thomsonreuters.upa.codec.QosTimeliness;
import com.thomsonreuters.upa.codec.StateCodes;
import com.thomsonreuters.upa.codec.StreamStates;
import com.thomsonreuters.upa.examples.common.ChannelSession;
import com.thomsonreuters.upa.examples.niprovider.StreamIdWatchList.WatchListEntry;
import com.thomsonreuters.upa.shared.rdm.marketbyorder.MarketByOrderClose;
import com.thomsonreuters.upa.shared.rdm.marketbyorder.MarketByOrderRefresh;
import com.thomsonreuters.upa.shared.rdm.marketbyorder.MarketByOrderResponseBase;
import com.thomsonreuters.upa.shared.rdm.marketbyorder.MarketByOrderUpdate;
import com.thomsonreuters.upa.rdm.DomainTypes;
import com.thomsonreuters.upa.transport.Error;
import com.thomsonreuters.upa.transport.TransportBuffer;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.Service;

/**
 * This is the market by order handler for the UPA NIProvider application. It
 * provides methods to encode and send refreshes and updates, as well as
 * closing streams.
 */
public class MarketByOrderHandler
{
    public static int TRANSPORT_BUFFER_SIZE_REQUEST = ChannelSession.MAX_MSG_SIZE;
    public static int TRANSPORT_BUFFER_SIZE_CLOSE = ChannelSession.MAX_MSG_SIZE;

    private int domainType;

    private MarketByOrderRefresh marketByOrderRefresh;
    private MarketByOrderUpdate marketByOrderUpdate;
    private MarketByOrderClose closeMessage;

    private final StreamIdWatchList watchList; // stream states based on
                                               // response

    // reusable variables used for encoding
    protected FieldList fieldList = CodecFactory.createFieldList();
    protected FieldEntry fieldEntry = CodecFactory.createFieldEntry();
    private EncodeIterator encIter = CodecFactory.createEncodeIterator();

    public MarketByOrderHandler(StreamIdWatchList watchList, DataDictionary dictionary)
    {
        this(watchList, DomainTypes.MARKET_BY_ORDER, dictionary);
    }

    protected MarketByOrderHandler(StreamIdWatchList watchList, int domainType, DataDictionary dictionary)
    {
        this.watchList = watchList;
        this.domainType = domainType;
        marketByOrderRefresh = createMarketByOrderRefresh();
        marketByOrderRefresh.dictionary(dictionary);
        marketByOrderUpdate = createMarketByOrderUpdate();
        marketByOrderUpdate.dictionary(dictionary);
        closeMessage = new MarketByOrderClose();
    }

    protected MarketByOrderRefresh createMarketByOrderRefresh()
    {
        return new MarketByOrderRefresh();
    }
    
    protected MarketByOrderUpdate createMarketByOrderUpdate()
    {
        return new MarketByOrderUpdate();
    }
    
    private int closeStream(ChannelSession chnl, int streamId, Error error)
    {
        //get a buffer for the item close
        TransportBuffer msgBuf = chnl.getTransportBuffer(TRANSPORT_BUFFER_SIZE_CLOSE, false, error);
        if (msgBuf == null)
            return CodecReturnCodes.FAILURE;

        //encode item close
        closeMessage.clear();
        closeMessage.streamId(streamId);
        encIter.clear();
        encIter.setBufferAndRWFVersion(msgBuf, chnl.channel().majorVersion(), chnl.channel().minorVersion());

        int ret = closeMessage.encode(encIter);
        if (ret != CodecReturnCodes.SUCCESS)
        {
            System.out.println("encodeMarketByOrderClose(): Failed <"
                        + CodecReturnCodes.toString(ret) + ">");
        }
        return chnl.write(msgBuf, error);
    }

    /**
     * Close all item streams.
     * 
     * @param chnl The channel to send a item stream close to.
     * @param error Populated if an error occurs.
     */
    public int closeStreams(ChannelSession chnl, Error error)
    {
        Iterator<Entry<Integer, WatchListEntry>> iter = watchList.iterator();
        while(iter.hasNext())
        {
            Map.Entry<Integer, WatchListEntry> entry = iter.next();
         
            if (entry.getValue().domainType != domainType)
            {
                /* this entry is from a different domainType, skip */
                continue;
            }

            closeStream(chnl, entry.getKey().intValue(), error);
            iter.remove();
        }

        watchList.clear();

        return CodecReturnCodes.SUCCESS;
    }
    
    /**
     * Encodes and sends item refreshes for market by order domain.
     * 
     * @param chnl The channel to send a refresh to.
     * @param itemNames List of item names.
     * @param error Populated if an error occurs.
     * 
     * @return success if item refreshes can be made, can be encoded and sent
     *         successfully. Failure if encoding/sending refreshes failed.
     */
    public int sendItemRefreshes(ChannelSession chnl, List<String> itemNames, Service serviceInfo,
            Error error)
    {
        if (itemNames == null || itemNames.isEmpty())
            return CodecReturnCodes.SUCCESS;

        generateRefreshAndUpdate(serviceInfo);

        return sendRefreshes(chnl, itemNames, error);
    }

    private void generateRefreshAndUpdate(Service serviceInfo)
    {
        //refresh complete
        marketByOrderRefresh.applyRefreshComplete();

        //service Id
        marketByOrderRefresh.serviceId(serviceInfo.serviceId());
        marketByOrderRefresh.applyHasServiceId();
        marketByOrderUpdate.serviceId(serviceInfo.serviceId());
        marketByOrderUpdate.applyHasServiceId();

        //QoS
        marketByOrderRefresh.qos().dynamic(false);
        marketByOrderRefresh.qos().timeliness(QosTimeliness.REALTIME);
        marketByOrderRefresh.qos().rate(QosRates.TICK_BY_TICK);
        marketByOrderRefresh.applyHasQos();

        //state
        marketByOrderRefresh.state().streamState(StreamStates.OPEN);
        marketByOrderRefresh.state().dataState(DataStates.OK);
        marketByOrderRefresh.state().code(StateCodes.NONE);
        marketByOrderRefresh.state().text().data("Item Refresh Completed");
    }

    private int sendRefreshes(ChannelSession chnl, List<String> itemNames, Error error)
    {
        int ret = CodecReturnCodes.SUCCESS;
        for (String itemName : itemNames)
        {
            Integer streamId = watchList.add(domainType, itemName);

            marketByOrderRefresh.itemName().data(itemName);
            marketByOrderRefresh.streamId(streamId);
            marketByOrderRefresh
                    .marketByOrderItem(watchList.get(streamId).marketByOrderItem);

            ret = encodeAndSendContent(chnl, marketByOrderRefresh, error);
            if (ret < CodecReturnCodes.SUCCESS)
                return ret;
        }

        return CodecReturnCodes.SUCCESS;
    }

    /**
     * Encodes and sends item updates for market by order domain.
     * 
     * @param chnl The channel to send a refresh to.
     * @param error Populated if an error occurs.
     * 
     * @return success if item updates can be made, can be encoded and sent
     *         successfully. Failure if encoding/sending updates failed.
     */
    public int sendItemUpdates(ChannelSession chnl, Error error)
    {
        int ret = CodecReturnCodes.SUCCESS;
        for (Entry<Integer, WatchListEntry> mapEntry : watchList)
        {
            WatchListEntry wle = mapEntry.getValue();
            if (mapEntry.getValue().domainType != domainType)
            {
                /* this entry is from a different domainType, skip */
                continue;
            }
            /* update fields */
            wle.marketByOrderItem.updateFields();

            marketByOrderUpdate.streamId(mapEntry.getKey().intValue());
            marketByOrderUpdate.itemName().data(wle.itemName);
            marketByOrderUpdate.marketByOrderItem(wle.marketByOrderItem);

            ret = encodeAndSendContent(chnl, marketByOrderUpdate, error);
            if (ret < CodecReturnCodes.SUCCESS)
                return ret;
        }

        return ret;
    }

    private int encodeAndSendContent(ChannelSession chnl, MarketByOrderResponseBase marketContent,
            Error error)
    {
        //get a buffer for the item request
        TransportBuffer msgBuf = chnl.getTransportBuffer(TRANSPORT_BUFFER_SIZE_REQUEST, false,
                                                         error);

        if (msgBuf == null)
        {
            return CodecReturnCodes.FAILURE;
        }

        encIter.clear();
        encIter.setBufferAndRWFVersion(msgBuf, chnl.channel().majorVersion(), chnl.channel().minorVersion());

        int ret = marketContent.encode(encIter);
        if (ret < CodecReturnCodes.SUCCESS)
        {
            error.text("MarketByOrderResponse.encode failed");
            error.errorId(ret);
            return ret;
        }

        return chnl.write(msgBuf, error);
    }

}
