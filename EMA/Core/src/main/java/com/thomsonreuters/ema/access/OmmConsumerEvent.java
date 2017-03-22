///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2015. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.access;

/**
 * OmmConsumerEvent encapsulates item identifiers.
 * 
 * <p>OmmConsumerEvent is used to convey item identifiers to application.<br>
 * OmmConsumerEvent is returned through OmmConsumerClient callback methods.</p>
 * 
 * <p>OmmConsumerEvent is read only. It is used for item identification only.</p>
 * 
 * @see OmmConsumer
 * @see OmmConsumerClient
 */
public interface OmmConsumerEvent
{
	/**
	 * Returns a unique item identifier (a.k.a., item handle) associated by EMA with an open item stream.<br>
	 * Item identifier is returned from all OmmConsumer registerClient() methods.
	 * 
	 * @return item identifier or handle
	 */
	public long handle();

	/**
	 * Returns an identifier (a.k.a., closure) associated with an open stream by consumer application.<br>
	 * Application associates the closure with an open item stream on<br>
	 * OmmConsumer registerClient(... , ... , Object closure, ...) methods
	 * 
	 * @return closure value
	 */
	public Object closure();

	/**
	 * Returns current item's parent item identifier (a.k.a. parent item handle).<br>
	 * Application specifies parent item identifier on<br>
	 * OmmConsumer registerClient(... , ... , ... , long parentHandle) methods.
	 * 
	 * @return parent item identifier or parent handle
	*/
	public long parentHandle();
}