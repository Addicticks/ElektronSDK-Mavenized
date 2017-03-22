package com.thomsonreuters.upa.valueadd.reactor;

import com.thomsonreuters.upa.rdm.ClassesOfService;

/**
 * Flow control class of service.
 * 
 * @see ClassOfService
 * @see ClassesOfService
 */
public class CosFlowControl
{
    int _type = ClassesOfService.FlowControlTypes.NONE;
    int _recvWindowSize = -1;
    int _sendWindowSize;
    
    /**
     * Returns the type of the flow control class of service.
     * 
     * @see ClassesOfService
     */
    public int type()
    {
        return _type;
    }
    
    /**
     * Sets the type of the flow control class of service.
     * 
     * @see ClassesOfService
     */
    public void type(int type)
    {
        _type = type;
    }

    /**
     * Returns the receive window size of the flow control class of service.
     * This is the largest amount of data that the remote end of the stream
     * should send at any time when performing flow control.
     * 
     * @see ClassesOfService
     */
    public int recvWindowSize()
    {
        return _recvWindowSize;
    }

    /**
     * Sets the receive window size of the flow control class of service.
     * This is the largest amount of data that the remote end of the stream
     * should send at any time when performing flow control. Must be 0 or a
     * positive number up to 2,147,483,647.
     * 
     * @see ClassesOfService
     */
    public void recvWindowSize(int recvWindowSize)
    {
        _recvWindowSize = recvWindowSize;
    }

    /**
     * Returns the send window size of the flow control class of service.
     * This is the largest amount of data that this end of the stream should
     * send at any time when performing flow control.
     * 
     * @see ClassesOfService
     */
    public int sendWindowSize()
    {
        return _sendWindowSize;
    }

    void sendWindowSize(int sendWindowSize)
    {
        _sendWindowSize = sendWindowSize;
    }

    /**
     * Clears the CosFlowControl for re-use.
     */
    public void clear()
    {
        _type = ClassesOfService.FlowControlTypes.NONE;
        _recvWindowSize = -1;
        _sendWindowSize = -1;
    }
}
