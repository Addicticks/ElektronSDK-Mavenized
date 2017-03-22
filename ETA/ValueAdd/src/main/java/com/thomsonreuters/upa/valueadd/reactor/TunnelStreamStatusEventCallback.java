package com.thomsonreuters.upa.valueadd.reactor;

/**
 * The tunnel stream status event callback is used to communicate tunnel
 * stream status events to the application.
 */
public interface TunnelStreamStatusEventCallback
{
    /**
     * A callback function that the {@link Reactor} will use to communicate
     * tunnel stream status events to the application.
     * 
     * @param event A TunnelStreamStatusEvent containing event information. The
     *            TunnelStreamStatusEvent is valid only during callback
     *            
     * @return ReactorCallbackReturnCodes A callback return code that can
     *         trigger specific Reactor behavior based on the outcome of the
     *         callback function
     *         
     * @see TunnelStream
     * @see TunnelStreamStatusEvent
     * @see ReactorCallbackReturnCodes
     */
    public int statusEventCallback(TunnelStreamStatusEvent event);
}
