package com.thomsonreuters.upa.valueadd.reactor;

import java.util.HashMap;
import java.util.LinkedList;

import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.valueadd.common.VaNode;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryMsgFactory;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.Service;

/* Watchlist service that contains necessary information regarding the service. */
public class WlService extends VaNode
{
    Service _rdmService = DirectoryMsgFactory.createService();
    long _numOutstandingRequests; // number of outstanding requests for service
    
    /* waiting request list to handle cases where request could not be submitted with
     * directory stream up but the open window wasn't open */
    LinkedList<WlRequest> _waitingRequestList = new LinkedList<WlRequest>();
    
    /* watchlist streams associated with this service */
    LinkedList<WlStream> _streamList = new LinkedList<WlStream>(); 
    
    /* Table of item groups, by ID.  */
    HashMap<Buffer, WlItemGroup> _itemGroupTable = new HashMap<Buffer, WlItemGroup>();
    
    /* Linked list of item groups that are contained in itemGroupTable */
    LinkedList<Buffer> _itemGroupList = new LinkedList<Buffer>();
    
    WlInteger _tableKey;
    
    /* Returns the RDM service. Use to get or set RDM service. */
    Service rdmService()
    {
        return _rdmService;
    }

    /* Returns the number of outstanding requests. */
    long numOutstandingRequests()
    {
        return _numOutstandingRequests;
    }

    /* Sets the number of outstanding requests. */
    void numOutstandingRequests(long numOutstandingRequests)
    {
        _numOutstandingRequests = numOutstandingRequests;
    }
    
    /* Returns the waiting request list. Handle cases where request could not be
     * submitted with directory stream up but the open window wasn't open. */
    LinkedList<WlRequest> waitingRequestList()
    {
        return _waitingRequestList;
    }
    
    /* Returns the list of streams associated with this service. */
    LinkedList<WlStream> streamList()
    {
        return _streamList;
    }
    
    /*
     * Adds a wlItemGroup into wlItemGroupTable with the key provided.
     */
    void itemGroupTablePut(Buffer key, WlItemGroup wlItemGroup)
    {
    	_itemGroupList.add(key);
    	_itemGroupTable.put(key, wlItemGroup);
    }
    
    /*
     * Returns a wlItemGroup fomr the itemGroupTable based on the key.
     */
    WlItemGroup itemGroupTableGet(Buffer key)
    {
    	return _itemGroupTable.get(key);
    }
    
    /*
     * 	Removes a wlItemGroup from the itemGroupTable based on the key
     * 	and returns the previous value associated with key, or null if there was no mapping for key.
     */
    WlItemGroup itemGroupTableRemove(Buffer key)
    {
    	_itemGroupList.remove(key);
    	return _itemGroupTable.remove(key);
    }
    
    void tableKey(WlInteger tableKey)
    {
        _tableKey = tableKey;
    }
    
    WlInteger tableKey()
    {
        return _tableKey;
    }

    /* Clears the object for re-use. */
    void clear()
    {
        _numOutstandingRequests = 0;
        WlRequest wlRequest = null;
        while ((wlRequest = _waitingRequestList.poll()) != null)
        {
            wlRequest.returnToPool();
        }

        /*  Clear stream list (don't repool streams; item handler will already do that) */
        _streamList.clear();
        for (int i = 0; i < _itemGroupList.size(); ++i)
        {
        	_itemGroupTable.get(_itemGroupList.get(i)).returnToPool();
        }
        _itemGroupTable.clear();
        _itemGroupList.clear();
        _tableKey = null;
    }    
}
