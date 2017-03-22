package com.thomsonreuters.upa.valueadd.reactor;

/** ReactorRole base class. Used by all other role classes. */ 
public class ReactorRole
{
    int _type = 0; // ReactorRoleTypes
    ReactorChannelEventCallback _channelEventCallback = null;
    DefaultMsgCallback _defaultMsgCallback = null;

    /**
     * The role type.
     * 
     * @return the role type
     * 
     * @see ReactorRoleTypes
     */
    public int type()
    {
        return _type;
    }

    /**
     * The ReactorChannelEventCallback associated with this role. Handles channel
     * events. Must be provided for all roles.
     * 
     * @param callback
     */
    public void channelEventCallback(ReactorChannelEventCallback callback)
    {
        _channelEventCallback = callback;
    }
    
    /**
     * The ReactorChannelEventCallback associated with this role. Handles channel
     * events. Must be provided for all roles.
     * 
     * @return the channelEventCallback
     */
    public ReactorChannelEventCallback channelEventCallback()
    {
        return _channelEventCallback;
    }

    /**
     * The DefaultMsgCallback associated with this role. Handles message events
     * that aren't handled by a specific domain callback. Must be provided for
     * all roles.
     * 
     * @param callback
     */
    public void defaultMsgCallback(DefaultMsgCallback callback)
    {
        _defaultMsgCallback = callback;
    }
    
    /**
     * The DefaultMsgCallback associated with this role. Handles message events
     * that aren't handled by a specific domain callback. Must be provided for
     * all roles.
     * 
     * @return the defaultMsgCallback
     */
    public DefaultMsgCallback defaultMsgCallback()
    {
        return _defaultMsgCallback;
    }

    /**
     * Returns a String representation of this object.
     * 
     * @return a String representation of this object
     */
    public String toString()
    {
        return "ReactorRole: " + ReactorRoleTypes.toString(_type);
    }
}
