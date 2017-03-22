///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2015. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.access;

/**
 * OmmConsumerErrorclient class provides callback mechanism used in place of exceptions.
 * 
 * <p>By default OmmConsumer class throws exceptions if a usage error occurs.
 * <br>Specifying OmmConsumerErrorClient on the constructor of OmmConsumer overwrites this behaviour.
 * <br>Instead of throwing exceptions, respective callback method on OmmConsumerErrorClient will be invoked.</p>
 * 
 * @see OmmConsumer
 * @see OmmException
 * @see OmmInvalidUsageException
 * @see OmmInvalidHandleException
 */
public interface OmmConsumerErrorClient
{
	/**
	 * Invoked upon receiving an invalid handle.
	 * <br>Requires OmmConsumer constructor to have an OmmConsumerErrorClient.
	 * 
	 * @param handle value of the handle that is invalid
	 * @param text specifies associated error text
	 */
	public void onInvalidHandle(long handle, String text);

	/**
	 * Invoked in the case of invalid usage.
	 * <br>Requires OmmConsumer constructor to have an OmmConsumerErrorClient.
	 * 
	 * @param text specifies associated error text
	 */ 
	public void onInvalidUsage(String text);
}