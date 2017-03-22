package com.thomsonreuters.upa.valueadd.examples.provider;


import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.codec.CodecReturnCodes;
import com.thomsonreuters.upa.codec.DataDictionary;
import com.thomsonreuters.upa.codec.DataStates;
import com.thomsonreuters.upa.codec.Msg;
import com.thomsonreuters.upa.codec.QosRates;
import com.thomsonreuters.upa.codec.QosTimeliness;
import com.thomsonreuters.upa.codec.StateCodes;
import com.thomsonreuters.upa.codec.StreamStates;
import com.thomsonreuters.upa.shared.provider.ItemInfo;
import com.thomsonreuters.upa.shared.provider.ItemRequestInfo;
import com.thomsonreuters.upa.shared.provider.MarketByPriceItems;
import com.thomsonreuters.upa.shared.rdm.marketbyprice.MarketByPriceItem;
import com.thomsonreuters.upa.transport.Channel;
import com.thomsonreuters.upa.transport.Error;
import com.thomsonreuters.upa.transport.TransportBuffer;
import com.thomsonreuters.upa.valueadd.cache.PayloadEntry;
import com.thomsonreuters.upa.valueadd.examples.common.CacheHandler;
import com.thomsonreuters.upa.valueadd.examples.common.CacheInfo;

/**
 * Provides a method to encode msg from cached data.
 */
public class MarketByPriceCacheItems extends MarketByPriceItems
{
    /**
     * Encodes the market by price refresh. Returns success if encoding succeeds
     * or failure if encoding fails.
     * 
     * @param itemReqInfo - The item request related info
     * @param isSolicited - The response is solicited if set
     * @param msgBuf - The message buffer to encode the market price response
     *            into
     * @param serviceId - The service id of the market price response
     * @param dictionary - The dictionary used for encoding
     * @param error - error in case of encoding failure
     * @param cacheInfo - cache info to determine if cache data
     * @param cacheEntry - cache entry where to retrieve or apply data from/to cache
     * 
     * @return {@link CodecReturnCodes}
     */
    public int encodeRefresh(ItemRequestInfo itemReqInfo, TransportBuffer msgBuf, boolean isSolicited, int serviceId,
    		DataDictionary dictionary, int multiPartNo, Error error, CacheInfo cacheInfo, PayloadEntry cacheEntry)
    {
    	ItemInfo itemInfo = itemReqInfo.itemInfo();
    	Channel channel = itemReqInfo.channel();
    	boolean isPrivateStream = itemReqInfo.isPrivateStreamRequest();
    	boolean isStreaming = itemReqInfo.isStreamingRequest();
    	int streamId = itemReqInfo.streamId();
    	Buffer itemName = itemInfo.itemName();
    	int ret = CodecReturnCodes.SUCCESS;
    	
        _marketByPriceRefresh.clear();
        _marketByPriceRefresh.dictionary(dictionary);
        _marketByPriceRefresh.itemName().data(itemName.data(), itemName.position(), itemName.length());
        _marketByPriceRefresh.streamId(streamId);
        _marketByPriceRefresh.applyHasServiceId();
        _marketByPriceRefresh.serviceId(serviceId);
        if (isSolicited)
        {
            _marketByPriceRefresh.applySolicited();
            
            // set clear cache on first part of all solicted refreshes.
            if(multiPartNo == 0)
            {
                _marketByPriceRefresh.applyClearCache();
            }
        }

        if (isPrivateStream)
            _marketByPriceRefresh.applyPrivateStream();

        
        _marketByPriceRefresh.dictionary(dictionary);
        
        // QoS 
        _marketByPriceRefresh.qos().dynamic(false);
        _marketByPriceRefresh.qos().timeliness(QosTimeliness.REALTIME);
        _marketByPriceRefresh.qos().rate(QosRates.TICK_BY_TICK);
        _marketByPriceRefresh.applyHasQos();

        // State 
        _marketByPriceRefresh.state().streamState(isStreaming ? StreamStates.OPEN : StreamStates.NON_STREAMING);
        _marketByPriceRefresh.state().dataState(DataStates.OK);
        _marketByPriceRefresh.state().code(StateCodes.NONE);
        
        _marketByPriceRefresh.marketByPriceItem((MarketByPriceItem)itemInfo.itemData());
        
       	// multi-part refresh complete when multiPartNo hits max
        if (multiPartNo == MAX_ORDERS - 1)
        {
            _marketByPriceRefresh.applyRefreshComplete();
            _marketByPriceRefresh.state().text().data("Item Refresh Completed");
        }
        else
        {
            _marketByPriceRefresh.state().text().data("Item Refresh In Progress");
        }
        _marketByPriceRefresh.partNo(multiPartNo);
        
        _encodeIter.clear();
        ret = _encodeIter.setBufferAndRWFVersion(msgBuf, channel.majorVersion(), channel.minorVersion());
        if (ret != CodecReturnCodes.SUCCESS)
        {
            error.text("EncodeIterator.setBufferAndRWFVersion() failed with return code: " + CodecReturnCodes.toString(ret));
            return ret;
        }
        ret = _marketByPriceRefresh.encode(_encodeIter);
        if (ret != CodecReturnCodes.SUCCESS)
            error.text("MarketByPriceRefresh.encode() failed");
        else if ( cacheInfo.useCache ) 
        {
        	System.out.println("Applying item " + itemReqInfo.itemName() + " to cache.");
        	ret = CacheHandler.applyMsgBufferToCache(_encodeIter.majorVersion(), _encodeIter.minorVersion(), 
        										cacheEntry, cacheInfo, msgBuf);
        	if (ret != CodecReturnCodes.SUCCESS)
        		error.text(" Error Applying payload to cache : " + cacheInfo.cacheError.text());
        }
        
        return ret;
    }
    
    public int encodeRefreshFromCache(ItemRequestInfo itemReqInfo, TransportBuffer msgBuf, boolean isSolicited, int serviceId,
    		DataDictionary dictionary, int multiPartNo, Error error, CacheInfo cacheInfo, PayloadEntry cacheEntry)
    {
    	ItemInfo itemInfo = itemReqInfo.itemInfo();
    	Channel channel = itemReqInfo.channel();
    	boolean isPrivateStream = itemReqInfo.isPrivateStreamRequest();
    	boolean isStreaming = itemReqInfo.isStreamingRequest();
    	int streamId = itemReqInfo.streamId();
    	Buffer itemName = itemInfo.itemName();
    	int ret = CodecReturnCodes.SUCCESS;
    	
    	_marketByPriceRefresh.clear();
        _marketByPriceRefresh.dictionary(dictionary);
        _marketByPriceRefresh.itemName().data(itemName.data(), itemName.position(), itemName.length());
        _marketByPriceRefresh.streamId(streamId);
        _marketByPriceRefresh.applyHasServiceId();
        _marketByPriceRefresh.serviceId(serviceId);
        if (isSolicited)
        {
            _marketByPriceRefresh.applySolicited();
            
            // set clear cache on first part of all solicted refreshes.
            if(multiPartNo == 0)
            {
                _marketByPriceRefresh.applyClearCache();
            }
        }

        if (isPrivateStream)
            _marketByPriceRefresh.applyPrivateStream();

        
        _marketByPriceRefresh.dictionary(dictionary);
        
        // QoS 
        _marketByPriceRefresh.qos().dynamic(false);
        _marketByPriceRefresh.qos().timeliness(QosTimeliness.REALTIME);
        _marketByPriceRefresh.qos().rate(QosRates.TICK_BY_TICK);
        _marketByPriceRefresh.applyHasQos();

        // State 
        _marketByPriceRefresh.state().streamState(isStreaming ? StreamStates.OPEN : StreamStates.NON_STREAMING);
        _marketByPriceRefresh.state().dataState(DataStates.OK);
        _marketByPriceRefresh.state().code(StateCodes.NONE);
        _marketByPriceRefresh.state().text().data("Item Refresh");
        
        _marketByPriceRefresh.marketByPriceItem((MarketByPriceItem)itemInfo.itemData());
         
        _marketByPriceRefresh.partNo(multiPartNo);
        
        _encodeIter.clear();
        ret = _encodeIter.setBufferAndRWFVersion(msgBuf, channel.majorVersion(), channel.minorVersion());
        if (ret != CodecReturnCodes.SUCCESS)
        {
            error.text("EncodeIterator.setBufferAndRWFVersion() failed with return code: " + CodecReturnCodes.toString(ret));
            return ret;
        }	
        
        // set-up message
        Msg msg = _marketByPriceRefresh.encodeMsg();

        // encode message
        ret = msg.encodeInit(_encodeIter, 0);
        if (ret < CodecReturnCodes.SUCCESS)
            return ret;
   
    	ret = CacheHandler.retrieveFromCache(_encodeIter, cacheEntry, cacheInfo);
    	if (ret != CodecReturnCodes.SUCCESS)
    	{
    		error.text(" Error retrieving payload from cache : " + cacheInfo.cacheError.text());
    		return ret;
    	}
    	
    	if (cacheInfo.cursor.isComplete())
    		_encodeIter.setRefreshCompleteFlag();
    	
    	ret = msg.encodeComplete(_encodeIter, true);
    	if (ret < CodecReturnCodes.SUCCESS)
    		return ret;
    	
       	return ret;
    }

    /**
     * Encodes the market by price update. Returns success if encoding succeeds
     * or failure if encoding fails.
     * 
     * @param itemReqInfo - The item request related info
     * @param isSolicited - The response is solicited if set
     * @param msgBuf - The message buffer to encode the market price response into
     * @param serviceId - The service id of the market price response
     * @param dictionary - The dictionary used for encoding
     * @param error - error in case of encoding failure
     * @param cacheInfo - cache info to determine if cache data
     * @param cacheEntry - cache entry where to retrieve or apply data from/to cache
     * 
     * @return {@link CodecReturnCodes}
     */
    public int encodeUpdate(ItemRequestInfo itemReqInfo, TransportBuffer msgBuf, boolean isSolicited,
    					int serviceId, DataDictionary dictionary, Error error, CacheInfo cacheInfo, PayloadEntry cacheEntry)
    {
    	ItemInfo itemInfo = itemReqInfo.itemInfo();
    	Channel channel = itemReqInfo.channel();
    	boolean isPrivateStream = itemReqInfo.isPrivateStreamRequest();
    	int streamId = itemReqInfo.streamId();
    	Buffer itemName = itemInfo.itemName();
    	
    	
        _marketByPriceUpdate.clear();
        _marketByPriceUpdate.applyRefreshComplete();
        if(isPrivateStream)
            _marketByPriceUpdate.applyPrivateStream();
        
        _marketByPriceUpdate.dictionary(dictionary);
        _marketByPriceUpdate.itemName().data(itemName.data(), itemName.position(), itemName.length());
        _marketByPriceUpdate.streamId(streamId);
        _marketByPriceUpdate.dictionary(dictionary);
        _marketByPriceUpdate.marketByPriceItem((MarketByPriceItem)itemInfo.itemData());
        _encodeIter.clear();
        int ret = _encodeIter.setBufferAndRWFVersion(msgBuf, channel.majorVersion(), channel.minorVersion());
        if (ret != CodecReturnCodes.SUCCESS)
        {
            error.text("EncodeIterator.setBufferAndRWFVersion() failed with return code: " + CodecReturnCodes.toString(ret));
            return ret;
        }
        ret = _marketByPriceUpdate.encode(_encodeIter);
        if (ret != CodecReturnCodes.SUCCESS)
            error.text("MarketByPriceUpdate.encode() failed");
        else if ( cacheInfo.useCache ) 
        {
        	System.out.println("Applying item " + itemReqInfo.itemName() + " to cache.");
        	ret = CacheHandler.applyMsgBufferToCache(_encodeIter.majorVersion(), _encodeIter.minorVersion(), 
        										cacheEntry, cacheInfo, msgBuf);
        	if (ret != CodecReturnCodes.SUCCESS)
        		error.text(" Error Applying payload to cache : " + cacheInfo.cacheError.text());
        }

        return ret;
    }
}