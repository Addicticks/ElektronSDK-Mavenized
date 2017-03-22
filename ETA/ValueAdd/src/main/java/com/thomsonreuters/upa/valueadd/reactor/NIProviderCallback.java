package com.thomsonreuters.upa.valueadd.reactor;

/**
 * Callback used for processing all non-interactive provider events and messages.
 * 
 * @see ReactorChannelEventCallback
 * @see DefaultMsgCallback
 * @see RDMLoginMsgCallback
 */
public interface NIProviderCallback extends ReactorChannelEventCallback, DefaultMsgCallback, RDMLoginMsgCallback
{

}
