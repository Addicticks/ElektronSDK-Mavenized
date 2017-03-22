package com.thomsonreuters.upa.valueadd.domainrep.rdm;

import com.thomsonreuters.upa.codec.DecodeIterator;
import com.thomsonreuters.upa.codec.EncodeIterator;
import com.thomsonreuters.upa.codec.Msg;

/**
 * This message structure contains the information that is common across all RDM
 * Message formats. It is included in all Value Added RDM Components.
 */
public interface MsgBase
{
    /**
     * The Stream Id for the given item.
     * 
     * @return the streamId
     */
    public int streamId();

    /**
     * The Stream Id for the given item.
     *
     * @param streamId the streamId to set
     * 
     */
    public void streamId(int streamId);
    
    /**
     * Returns the domain type of the RDM message.
     */
    public int domainType();

    /**
     * Encode an RDM message.
     * 
     * @param eIter The Encode Iterator
     * 
     * @return UPA return value
     */
    public int encode(EncodeIterator eIter);

    /**
     * Decode a UPA message into an RDM message.
     * 
     * @param dIter The Decode Iterator
     * 
     * @return UPA return value
     */
    public int decode(DecodeIterator dIter, Msg msg);

    /**
     * Clears the current contents of the message and prepares it for re-use.
     */
    public void clear();
}